package com.acme.transport.crewing.swap.score

import com.acme.transport.kit.text.normalize

/** A rule a swap reservation must pass. Check returns its problems, or none. */
interface SwapReservationRule {
    fun check(item: SwapReservation): MutableList<String>
}

data class SwapReservationChecksumLengthRule(var maxLength: Int) : SwapReservationRule {
    override fun check(item: SwapReservation): MutableList<String> {
        val size = item.checksum.length
        if (size > maxLength) {
            return mutableListOf<String>("checksum is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class SwapReservationAllowedLocaleRule(var allowed: MutableList<String>) : SwapReservationRule {
    override fun check(item: SwapReservation): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.locale))) {
            return mutableListOf<String>("locale ${item.locale} is expired")
        }
        return mutableListOf<String>()
    }
}

data class SwapReservationRevisionRangeRule(var min: Int, var max: Int) : SwapReservationRule {
    override fun check(item: SwapReservation): MutableList<String> {
        val value = item.revision
        if (value < min || value > max) {
            return mutableListOf<String>("revision $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class SwapReservationRequiredChecksumRule(var message: String) : SwapReservationRule {
    override fun check(item: SwapReservation): MutableList<String> {
        if (item.checksum.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a swap reservation. */
data class SwapReservationValidator(var rules: MutableList<SwapReservationRule>) {
    fun validate(item: SwapReservation): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            val found = rule.check(item)
            if (found.size == 0) {
                continue
            }
            for (problem in found) {
                problems.add("${item.code}: $problem")
            }
        }
        return problems
    }

    fun requireValid(item: SwapReservation) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<SwapReservation>): MutableMap<String, MutableList<String>> {
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

fun defaultSwapReservationValidator(): SwapReservationValidator {
    return SwapReservationValidator(
        mutableListOf<SwapReservationRule>(
            SwapReservationChecksumLengthRule(3600),
            SwapReservationAllowedLocaleRule(mutableListOf<String>("strict", "default")),
            SwapReservationRevisionRangeRule(32, 80),
            SwapReservationRequiredChecksumRule("swap reservation needs a value"),
        ),
    )
}
