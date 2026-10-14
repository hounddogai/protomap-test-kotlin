package com.acme.transport.customs.declaration.milestone

import com.acme.transport.kit.text.normalize

/** A rule a declaration reservation must pass. Check returns its problems, or none. */
interface DeclarationReservationRule {
    fun check(item: DeclarationReservation): MutableList<String>
}

data class DeclarationReservationDurationMinutesRangeRule(
    var min: Int,
    var max: Int,
) : DeclarationReservationRule {
    override fun check(item: DeclarationReservation): MutableList<String> {
        val value = item.durationMinutes
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("duration minutes $value is below $min")
        }
        if (value > max) {
            problems.add("duration minutes $value is above $max")
        }
        return problems
    }
}

data class DeclarationReservationLabelLengthRule(var maxLength: Int) : DeclarationReservationRule {
    override fun check(item: DeclarationReservation): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DeclarationReservationRequiredLocaleRule(var message: String) : DeclarationReservationRule {
    override fun check(item: DeclarationReservation): MutableList<String> {
        if (item.locale.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DeclarationReservationAttemptsRangeRule(var min: Int, var max: Int) : DeclarationReservationRule {
    override fun check(item: DeclarationReservation): MutableList<String> {
        val value = item.attempts
        if (value < min || value > max) {
            return mutableListOf<String>("attempts $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class DeclarationReservationAllowedCodeRule(
    var allowed: MutableList<String>,
) : DeclarationReservationRule {
    override fun check(item: DeclarationReservation): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is above maximum")
        }
        return mutableListOf<String>()
    }
}

data class DeclarationReservationCodeLengthRule(var maxLength: Int) : DeclarationReservationRule {
    override fun check(item: DeclarationReservation): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a declaration reservation. */
data class DeclarationReservationValidator(var rules: MutableList<DeclarationReservationRule>) {
    fun validate(item: DeclarationReservation): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: DeclarationReservation) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DeclarationReservation>): MutableMap<String, MutableList<String>> {
        val report = mutableMapOf<String, MutableList<String>>()
        for (item in items) {
            val problems = validate(item)
            if (problems.size > 0) {
                report[item.code] = problems
            }
        }
        return report
    }
}

fun defaultDeclarationReservationValidator(): DeclarationReservationValidator {
    return DeclarationReservationValidator(
        mutableListOf<DeclarationReservationRule>(
            DeclarationReservationDurationMinutesRangeRule(32, 1000),
            DeclarationReservationLabelLengthRule(80),
            DeclarationReservationRequiredLocaleRule("declaration reservation needs this field"),
            DeclarationReservationAttemptsRangeRule(32, 255),
            DeclarationReservationAllowedCodeRule(mutableListOf<String>("fallback")),
            DeclarationReservationCodeLengthRule(120),
        ),
    )
}
