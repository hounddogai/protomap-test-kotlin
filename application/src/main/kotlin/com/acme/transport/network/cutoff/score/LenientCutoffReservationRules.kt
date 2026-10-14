package com.acme.transport.network.cutoff.score

import com.acme.transport.kit.text.normalize

/** A rule a lenient cutoff reservation must pass. Check returns its problems, or none. */
interface LenientCutoffReservationRule {
    fun check(item: LenientCutoffReservation): MutableList<String>
}

data class LenientCutoffReservationAllowedCodeRule(
    var allowed: MutableList<String>,
) : LenientCutoffReservationRule {
    override fun check(item: LenientCutoffReservation): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is duplicated")
        }
        return mutableListOf<String>()
    }
}

data class LenientCutoffReservationAttemptsRangeRule(
    var min: Int,
    var max: Int,
) : LenientCutoffReservationRule {
    override fun check(item: LenientCutoffReservation): MutableList<String> {
        val value = item.attempts
        if (value < min || value > max) {
            return mutableListOf<String>("attempts $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class LenientCutoffReservationUpdatedAtRangeRule(
    var min: Int,
    var max: Int,
) : LenientCutoffReservationRule {
    override fun check(item: LenientCutoffReservation): MutableList<String> {
        val value = item.updatedAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("updated at $value is below $min")
        }
        if (value > max) {
            problems.add("updated at $value is above $max")
        }
        return problems
    }
}

data class LenientCutoffReservationCodeLengthRule(var maxLength: Int) : LenientCutoffReservationRule {
    override fun check(item: LenientCutoffReservation): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LenientCutoffReservationSequenceRangeRule(
    var min: Int,
    var max: Int,
) : LenientCutoffReservationRule {
    override fun check(item: LenientCutoffReservation): MutableList<String> {
        val value = item.sequence
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("sequence $value is below $min")
        }
        if (value > max) {
            problems.add("sequence $value is above $max")
        }
        return problems
    }
}

data class LenientCutoffReservationCodePrefixRule(var prefix: String) : LenientCutoffReservationRule {
    override fun check(item: LenientCutoffReservation): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a lenient cutoff reservation. */
data class LenientCutoffReservationValidator(var rules: MutableList<LenientCutoffReservationRule>) {
    fun validate(item: LenientCutoffReservation): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: LenientCutoffReservation) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LenientCutoffReservation>): MutableMap<String, MutableList<String>> {
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

fun defaultLenientCutoffReservationValidator(): LenientCutoffReservationValidator {
    return LenientCutoffReservationValidator(
        mutableListOf<LenientCutoffReservationRule>(
            LenientCutoffReservationAllowedCodeRule(mutableListOf<String>()),
            LenientCutoffReservationAttemptsRangeRule(1000, 86400),
            LenientCutoffReservationUpdatedAtRangeRule(255, 3600),
            LenientCutoffReservationCodeLengthRule(120),
            LenientCutoffReservationSequenceRangeRule(1000, 86400),
            LenientCutoffReservationCodePrefixRule("X"),
        ),
    )
}
