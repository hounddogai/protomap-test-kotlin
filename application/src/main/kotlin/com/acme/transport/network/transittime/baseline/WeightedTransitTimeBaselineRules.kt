package com.acme.transport.network.transittime.baseline

/** A rule a weighted transit time baseline must pass. Check returns its problems, or none. */
interface WeightedTransitTimeBaselineRule {
    fun check(item: WeightedTransitTimeBaseline): MutableList<String>
}

data class WeightedTransitTimeBaselineExpiresAtRangeRule(
    var min: Int,
    var max: Int,
) : WeightedTransitTimeBaselineRule {
    override fun check(item: WeightedTransitTimeBaseline): MutableList<String> {
        val value = item.expiresAt
        if (value < min || value > max) {
            return mutableListOf<String>("expires at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class WeightedTransitTimeBaselineCodePrefixRule(var prefix: String) : WeightedTransitTimeBaselineRule {
    override fun check(item: WeightedTransitTimeBaseline): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class WeightedTransitTimeBaselineTitleLengthRule(var maxLength: Int) : WeightedTransitTimeBaselineRule {
    override fun check(item: WeightedTransitTimeBaseline): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a weighted transit time baseline. */
data class WeightedTransitTimeBaselineValidator(var rules: MutableList<WeightedTransitTimeBaselineRule>) {
    fun validate(item: WeightedTransitTimeBaseline): MutableList<String> {
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

    fun requireValid(item: WeightedTransitTimeBaseline) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<WeightedTransitTimeBaseline>): MutableMap<String, MutableList<String>> {
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

fun defaultWeightedTransitTimeBaselineValidator(): WeightedTransitTimeBaselineValidator {
    return WeightedTransitTimeBaselineValidator(
        mutableListOf<WeightedTransitTimeBaselineRule>(
            WeightedTransitTimeBaselineExpiresAtRangeRule(255, 3600),
            WeightedTransitTimeBaselineCodePrefixRule("ACM"),
            WeightedTransitTimeBaselineTitleLengthRule(120),
        ),
    )
}
