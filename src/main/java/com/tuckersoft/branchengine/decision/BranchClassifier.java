package com.tuckersoft.branchengine.decision;

import java.text.Normalizer;
import java.util.regex.Pattern;

/** Clasifica el texto del jugador con reglas propias, en el orden del enunciado. */
public final class BranchClassifier {

    public static final String OBEDIENCIA = "OBEDIENCIA";
    public static final String REBELDIA = "REBELDIA";
    public static final String SOSPECHA = "SOSPECHA";
    public static final String RUPTURA_CUARTA_PARED = "RUPTURA_CUARTA_PARED";
    public static final String ENTRADA_CORRUPTA = "ENTRADA_CORRUPTA";

    private static final Pattern LETRA = Pattern.compile("[a-z]");

    public static String classify(String rawInput) {
        String texto = Normalizer.normalize(rawInput, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase();
        if (!LETRA.matcher(texto).find()) {
            return ENTRADA_CORRUPTA;
        }
        if (contieneAlguna(texto, "netflix", "camara", "espectador", "videojuego")) {
            return RUPTURA_CUARTA_PARED;
        }
        if (contieneAlguna(texto, "vigilan", "simbolo", "conspiracion")) {
            return SOSPECHA;
        }
        if (contieneAlguna(texto, "rechaza", "destruye", "desobedece", "renuncia")) {
            return REBELDIA;
        }
        return OBEDIENCIA;
    }

    public static String handlerUnit(String branchType) {
        return switch (branchType) {
            case REBELDIA -> "Control de Continuidad";
            case SOSPECHA -> "Oficina de Seguridad";
            case RUPTURA_CUARTA_PARED -> "Departamento Netflix";
            case ENTRADA_CORRUPTA -> "Archivo de Errores";
            default -> "Mesa de Guion";
        };
    }

    public static String outcomeCode(String branchType) {
        return switch (branchType) {
            case REBELDIA -> "FORK_TIMELINE";
            case SOSPECHA -> "INJECT_WHITE_BEAR_SYMBOL";
            case RUPTURA_CUARTA_PARED -> "BREAK_FOURTH_WALL";
            case ENTRADA_CORRUPTA -> "DISCARD_INPUT";
            default -> "ADVANCE_MAIN_PATH";
        };
    }

    private static boolean contieneAlguna(String texto, String... palabras) {
        for (String p : palabras) {
            if (texto.contains(p)) {
                return true;
            }
        }
        return false;
    }

    private BranchClassifier() {
    }
}
