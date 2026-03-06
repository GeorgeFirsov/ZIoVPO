package com.example.signature;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

@Service
public class JcsCanonicalizationService implements CanonicalizationService {

    private final ObjectMapper objectMapper;

    public JcsCanonicalizationService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public CanonicalBytes canonicalize(Object payload) {
        if (payload == null) {
            throw new SignatureModuleException(SignatureErrorCode.INPUT_INVALID, "payload is null");
        }

        try {
            JsonNode node = objectMapper.valueToTree(payload);
            StringBuilder sb = new StringBuilder();
            writeCanonical(node, sb);
            return new CanonicalBytes(sb.toString().getBytes(StandardCharsets.UTF_8));
        } catch (SignatureModuleException e) {
            throw e;
        } catch (Exception e) {
            throw new SignatureModuleException(SignatureErrorCode.CANONICALIZATION_FAILED, "canonicalization failed", e);
        }
    }

    private void writeCanonical(JsonNode node, StringBuilder out) {
        if (node == null || node.isMissingNode() || node.isNull()) {
            out.append("null");
            return;
        }

        if (node.isObject()) {
            out.append('{');

            List<String> names = new ArrayList<>();
            Iterator<String> it = node.fieldNames();
            while (it.hasNext()) {
                names.add(it.next());
            }
            Collections.sort(names);

            for (int i = 0; i < names.size(); i++) {
                if (i > 0) {
                    out.append(',');
                }

                String name = names.get(i);
                out.append('\"');
                out.append(escapeJsonString(name));
                out.append('\"');
                out.append(':');

                writeCanonical(node.get(name), out);
            }

            out.append('}');
            return;
        }

        if (node.isArray()) {
            out.append('[');
            for (int i = 0; i < node.size(); i++) {
                if (i > 0) {
                    out.append(',');
                }
                writeCanonical(node.get(i), out);
            }
            out.append(']');
            return;
        }

        if (node.isTextual()) {
            out.append('\"');
            out.append(escapeJsonString(node.asText()));
            out.append('\"');
            return;
        }

        if (node.isBoolean()) {
            out.append(node.asBoolean() ? "true" : "false");
            return;
        }

        if (node.isNumber()) {
            out.append(formatNumberJcs(node));
            return;
        }

        throw new SignatureModuleException(SignatureErrorCode.INPUT_INVALID,
                "unsupported json node type: " + node.getNodeType());
    }

    private String formatNumberJcs(JsonNode node) {
        BigDecimal original;
        try {
            original = node.decimalValue();
        } catch (Exception e) {
            throw new SignatureModuleException(SignatureErrorCode.INPUT_INVALID, "invalid number");
        }

        if (original == null) {
            throw new SignatureModuleException(SignatureErrorCode.INPUT_INVALID, "invalid number");
        }

        if (node.isFloatingPointNumber()) {
            double d = node.doubleValue();
            if (Double.isNaN(d) || Double.isInfinite(d)) {
                throw new SignatureModuleException(SignatureErrorCode.INPUT_INVALID, "NaN/Infinity is not allowed");
            }
        }

        BigDecimal normalizedOriginal = original.stripTrailingZeros();
        double d = normalizedOriginal.doubleValue();

        if (Double.isNaN(d) || Double.isInfinite(d)) {
            throw new SignatureModuleException(SignatureErrorCode.INPUT_INVALID, "NaN/Infinity is not allowed");
        }

        BigDecimal roundTrip = BigDecimal.valueOf(d).stripTrailingZeros();
        if (roundTrip.compareTo(normalizedOriginal) != 0) {
            throw new SignatureModuleException(SignatureErrorCode.INPUT_INVALID,
                    "number is not IEEE-754 double compatible (I-JSON)");
        }

        if (d == 0.0d) {
            return "0";
        }

        double abs = Math.abs(d);

        if (abs >= 1e21d || abs < 1e-6d) {
            return toScientificJcs(d);
        }

        return toPlainJcs(d);
    }

    private String toPlainJcs(double d) {
        BigDecimal bd = BigDecimal.valueOf(d).stripTrailingZeros();
        String s = bd.toPlainString();
        if ("-0".equals(s)) {
            return "0";
        }
        return s;
    }

    private String toScientificJcs(double d) {
        BigDecimal bd = BigDecimal.valueOf(d).stripTrailingZeros();

        String sign = "";
        if (bd.signum() < 0) {
            sign = "-";
            bd = bd.abs();
        }

        if (bd.compareTo(BigDecimal.ZERO) == 0) {
            return "0";
        }

        int exponent = bd.precision() - 1 - bd.scale();
        BigDecimal mantissa = bd.movePointLeft(exponent).stripTrailingZeros();

        String mantissaStr = mantissa.toPlainString();
        if ("-0".equals(mantissaStr)) {
            mantissaStr = "0";
        }

        String expSign = exponent >= 0 ? "+" : "";
        return sign + mantissaStr + "e" + expSign + exponent;
    }

    private String escapeJsonString(String s) {
        if (s == null) {
            return "";
        }

        checkSurrogates(s);

        StringBuilder out = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char ch = s.charAt(i);

            if (ch == '\"') {
                out.append("\\\"");
                continue;
            }
            if (ch == '\\') {
                out.append("\\\\");
                continue;
            }
            if (ch == '\b') {
                out.append("\\b");
                continue;
            }
            if (ch == '\t') {
                out.append("\\t");
                continue;
            }
            if (ch == '\n') {
                out.append("\\n");
                continue;
            }
            if (ch == '\f') {
                out.append("\\f");
                continue;
            }
            if (ch == '\r') {
                out.append("\\r");
                continue;
            }

            if (ch <= 0x1F) {
                out.append("\\u");
                out.append(toLowerHex4(ch));
                continue;
            }

            out.append(ch);
        }

        return out.toString();
    }

    private void checkSurrogates(String s) {
        for (int i = 0; i < s.length(); i++) {
            char ch = s.charAt(i);

            if (Character.isHighSurrogate(ch)) {
                if (i + 1 >= s.length()) {
                    throw new SignatureModuleException(SignatureErrorCode.INPUT_INVALID, "lone high surrogate");
                }

                char next = s.charAt(i + 1);
                if (!Character.isLowSurrogate(next)) {
                    throw new SignatureModuleException(SignatureErrorCode.INPUT_INVALID, "lone high surrogate");
                }

                i++;
                continue;
            }

            if (Character.isLowSurrogate(ch)) {
                throw new SignatureModuleException(SignatureErrorCode.INPUT_INVALID, "lone low surrogate");
            }
        }
    }

    private String toLowerHex4(int ch) {
        String hex = Integer.toHexString(ch);
        while (hex.length() < 4) {
            hex = "0" + hex;
        }
        if (hex.length() > 4) {
            hex = hex.substring(hex.length() - 4);
        }
        return hex.toLowerCase();
    }
}