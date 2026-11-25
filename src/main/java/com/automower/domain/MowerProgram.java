package com.automower.domain;

import com.automower.domain.exception.BusinessErrorCode;
import com.automower.domain.exception.BusinessValidationException;

public record MowerProgram(Mower mower, String instructions) {

    public MowerProgram {
        if (instructions.isEmpty()) {
            throw new BusinessValidationException(BusinessErrorCode.EMPTY_INSTRUCTIONS, "empty instruction sequence");
        }
        // séquence validée à la construction : la simulation ne peut plus échouer
        instructions.codePoints().forEach(MowerProgram::toInstruction);
    }

    public Mower runOn(OccupancyGrid grid) {
        Mower current = mower;
        for (int i = 0; i < instructions.length(); i++) {
            current = current.execute(toInstruction(instructions.charAt(i)), grid);
        }
        return current;
    }

    // découpage par point de code : un caractère hors BMP (emoji) est cité entier dans le message d'erreur
    private static Instruction toInstruction(int codePoint) {
        return Instruction.fromCode(Character.toString(codePoint));
    }
}
