package com.acme.transport.freightpay.ratecard.rule

import com.acme.transport.kit.text.normalize

/** A rule a lenient rate card entry must pass. Check returns its problems, or none. */
interface LenientRateCardEntryRule {
    fun check(item: LenientRateCardEntry): MutableList<String>
}

data class LenientRateCardEntryFuelLitersRangeRule(
    var min: Double,
    var max: Double,
) : LenientRateCardEntryRule {
    override fun check(item: LenientRateCardEntry): MutableList<String> {
        val value = item.fuelLiters
        if (value < min || value > max) {
            return mutableListOf<String>("fuel liters $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class LenientRateCardEntryRequiredCodeRule(var message: String) : LenientRateCardEntryRule {
    override fun check(item: LenientRateCardEntry): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LenientRateCardEntryGraceDaysRangeRule(var min: Int, var max: Int) : LenientRateCardEntryRule {
    override fun check(item: LenientRateCardEntry): MutableList<String> {
        val value = item.graceDays
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("grace days $value is below $min")
        }
        if (value > max) {
            problems.add("grace days $value is above $max")
        }
        return problems
    }
}

data class LenientRateCardEntryCodeLengthRule(var maxLength: Int) : LenientRateCardEntryRule {
    override fun check(item: LenientRateCardEntry): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LenientRateCardEntryAllowedCodeRule(var allowed: MutableList<String>) : LenientRateCardEntryRule {
    override fun check(item: LenientRateCardEntry): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is below minimum")
        }
        return mutableListOf<String>()
    }
}

data class LenientRateCardEntryNameLengthRule(var maxLength: Int) : LenientRateCardEntryRule {
    override fun check(item: LenientRateCardEntry): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a lenient rate card entry. */
data class LenientRateCardEntryValidator(var rules: MutableList<LenientRateCardEntryRule>) {
    fun validate(item: LenientRateCardEntry): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: LenientRateCardEntry) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LenientRateCardEntry>): MutableMap<String, MutableList<String>> {
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

fun defaultLenientRateCardEntryValidator(): LenientRateCardEntryValidator {
    return LenientRateCardEntryValidator(
        mutableListOf<LenientRateCardEntryRule>(
            LenientRateCardEntryFuelLitersRangeRule(5.4, 965.9),
            LenientRateCardEntryRequiredCodeRule("lenient rate card entry needs a value"),
            LenientRateCardEntryGraceDaysRangeRule(80, 255),
            LenientRateCardEntryCodeLengthRule(80),
            LenientRateCardEntryAllowedCodeRule(mutableListOf<String>()),
            LenientRateCardEntryNameLengthRule(1000),
        ),
    )
}
