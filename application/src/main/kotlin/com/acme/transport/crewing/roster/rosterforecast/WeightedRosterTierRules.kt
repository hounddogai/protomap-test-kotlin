package com.acme.transport.crewing.roster.rosterforecast

/** A rule a weighted roster tier must pass. Check returns its problems, or none. */
interface WeightedRosterTierRule {
    fun check(item: WeightedRosterTier): MutableList<String>
}

data class WeightedRosterTierRateRangeRule(var min: Double, var max: Double) : WeightedRosterTierRule {
    override fun check(item: WeightedRosterTier): MutableList<String> {
        val value = item.rate
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("rate $value is below $min")
        }
        if (value > max) {
            problems.add("rate $value is above $max")
        }
        return problems
    }
}

data class WeightedRosterTierTitleLengthRule(var maxLength: Int) : WeightedRosterTierRule {
    override fun check(item: WeightedRosterTier): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class WeightedRosterTierCodePrefixRule(var prefix: String) : WeightedRosterTierRule {
    override fun check(item: WeightedRosterTier): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a weighted roster tier. */
data class WeightedRosterTierValidator(var rules: MutableList<WeightedRosterTierRule>) {
    fun validate(item: WeightedRosterTier): MutableList<String> {
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

    fun requireValid(item: WeightedRosterTier) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<WeightedRosterTier>): MutableMap<String, MutableList<String>> {
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

fun defaultWeightedRosterTierValidator(): WeightedRosterTierValidator {
    return WeightedRosterTierValidator(
        mutableListOf<WeightedRosterTierRule>(
            WeightedRosterTierRateRangeRule(8.7, 342.4),
            WeightedRosterTierTitleLengthRule(32),
            WeightedRosterTierCodePrefixRule("X"),
        ),
    )
}
