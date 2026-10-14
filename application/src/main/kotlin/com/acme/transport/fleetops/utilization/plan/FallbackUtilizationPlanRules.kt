package com.acme.transport.fleetops.utilization.plan

/** A rule a fallback utilization plan must pass. Check returns its problems, or none. */
interface FallbackUtilizationPlanRule {
    fun check(item: FallbackUtilizationPlan): MutableList<String>
}

data class FallbackUtilizationPlanBudgetRangeRule(
    var min: Double,
    var max: Double,
) : FallbackUtilizationPlanRule {
    override fun check(item: FallbackUtilizationPlan): MutableList<String> {
        val value = item.budget
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("budget $value is below $min")
        }
        if (value > max) {
            problems.add("budget $value is above $max")
        }
        return problems
    }
}

data class FallbackUtilizationPlanRevisionRangeRule(var min: Int, var max: Int) : FallbackUtilizationPlanRule {
    override fun check(item: FallbackUtilizationPlan): MutableList<String> {
        val value = item.revision
        if (value < min || value > max) {
            return mutableListOf<String>("revision $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class FallbackUtilizationPlanNameLengthRule(var maxLength: Int) : FallbackUtilizationPlanRule {
    override fun check(item: FallbackUtilizationPlan): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FallbackUtilizationPlanCodePrefixRule(var prefix: String) : FallbackUtilizationPlanRule {
    override fun check(item: FallbackUtilizationPlan): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class FallbackUtilizationPlanWindowDaysRangeRule(
    var min: Int,
    var max: Int,
) : FallbackUtilizationPlanRule {
    override fun check(item: FallbackUtilizationPlan): MutableList<String> {
        val value = item.windowDays
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("window days $value is below $min")
        }
        if (value > max) {
            problems.add("window days $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a fallback utilization plan. */
data class FallbackUtilizationPlanValidator(var rules: MutableList<FallbackUtilizationPlanRule>) {
    fun validate(item: FallbackUtilizationPlan): MutableList<String> {
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

    fun requireValid(item: FallbackUtilizationPlan) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FallbackUtilizationPlan>): MutableMap<String, MutableList<String>> {
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

fun defaultFallbackUtilizationPlanValidator(): FallbackUtilizationPlanValidator {
    return FallbackUtilizationPlanValidator(
        mutableListOf<FallbackUtilizationPlanRule>(
            FallbackUtilizationPlanBudgetRangeRule(6.7, 4959.0),
            FallbackUtilizationPlanRevisionRangeRule(255, 86400),
            FallbackUtilizationPlanNameLengthRule(16),
            FallbackUtilizationPlanCodePrefixRule("OPS"),
            FallbackUtilizationPlanWindowDaysRangeRule(32, 32),
        ),
    )
}
