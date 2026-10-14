package com.acme.transport.safety.coaching.baseline

/** A rule a manual coaching baseline must pass. Check returns its problems, or none. */
interface ManualCoachingBaselineRule {
    fun check(item: ManualCoachingBaseline): MutableList<String>
}

data class ManualCoachingBaselineWeightRangeRule(
    var min: Double,
    var max: Double,
) : ManualCoachingBaselineRule {
    override fun check(item: ManualCoachingBaseline): MutableList<String> {
        val value = item.weight
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("weight $value is below $min")
        }
        if (value > max) {
            problems.add("weight $value is above $max")
        }
        return problems
    }
}

data class ManualCoachingBaselineScoreRangeRule(var min: Double, var max: Double) : ManualCoachingBaselineRule {
    override fun check(item: ManualCoachingBaseline): MutableList<String> {
        val value = item.score
        if (value < min || value > max) {
            return mutableListOf<String>("score $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class ManualCoachingBaselineExpiresAtRangeRule(var min: Int, var max: Int) : ManualCoachingBaselineRule {
    override fun check(item: ManualCoachingBaseline): MutableList<String> {
        val value = item.expiresAt
        if (value < min || value > max) {
            return mutableListOf<String>("expires at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class ManualCoachingBaselineCodePrefixRule(var prefix: String) : ManualCoachingBaselineRule {
    override fun check(item: ManualCoachingBaseline): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a manual coaching baseline. */
data class ManualCoachingBaselineValidator(var rules: MutableList<ManualCoachingBaselineRule>) {
    fun validate(item: ManualCoachingBaseline): MutableList<String> {
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

    fun requireValid(item: ManualCoachingBaseline) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ManualCoachingBaseline>): MutableMap<String, MutableList<String>> {
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

fun defaultManualCoachingBaselineValidator(): ManualCoachingBaselineValidator {
    return ManualCoachingBaselineValidator(
        mutableListOf<ManualCoachingBaselineRule>(
            ManualCoachingBaselineWeightRangeRule(9.8, 1747.2),
            ManualCoachingBaselineScoreRangeRule(8.6, 2396.1),
            ManualCoachingBaselineExpiresAtRangeRule(3600, 3600),
            ManualCoachingBaselineCodePrefixRule("ACM"),
        ),
    )
}
