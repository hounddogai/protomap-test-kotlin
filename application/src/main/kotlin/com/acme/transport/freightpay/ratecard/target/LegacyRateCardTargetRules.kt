package com.acme.transport.freightpay.ratecard.target

/** A rule a legacy rate card target must pass. Check returns its problems, or none. */
interface LegacyRateCardTargetRule {
    fun check(item: LegacyRateCardTarget): MutableList<String>
}

data class LegacyRateCardTargetCodePrefixRule(var prefix: String) : LegacyRateCardTargetRule {
    override fun check(item: LegacyRateCardTarget): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class LegacyRateCardTargetRatioRangeRule(var min: Double, var max: Double) : LegacyRateCardTargetRule {
    override fun check(item: LegacyRateCardTarget): MutableList<String> {
        val value = item.ratio
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("ratio $value is below $min")
        }
        if (value > max) {
            problems.add("ratio $value is above $max")
        }
        return problems
    }
}

data class LegacyRateCardTargetFuelLitersRangeRule(
    var min: Double,
    var max: Double,
) : LegacyRateCardTargetRule {
    override fun check(item: LegacyRateCardTarget): MutableList<String> {
        val value = item.fuelLiters
        if (value < min || value > max) {
            return mutableListOf<String>("fuel liters $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a legacy rate card target. */
data class LegacyRateCardTargetValidator(var rules: MutableList<LegacyRateCardTargetRule>) {
    fun validate(item: LegacyRateCardTarget): MutableList<String> {
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

    fun requireValid(item: LegacyRateCardTarget) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LegacyRateCardTarget>): MutableMap<String, MutableList<String>> {
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

fun defaultLegacyRateCardTargetValidator(): LegacyRateCardTargetValidator {
    return LegacyRateCardTargetValidator(
        mutableListOf<LegacyRateCardTargetRule>(
            LegacyRateCardTargetCodePrefixRule("X"),
            LegacyRateCardTargetRatioRangeRule(9.2, 2665.4),
            LegacyRateCardTargetFuelLitersRangeRule(1.8, 746.6),
        ),
    )
}
