package com.acme.transport.fleetops.utilization.utilizationplan

/** A rule a manual utilization plan must pass. Check returns its problems, or none. */
interface ManualUtilizationPlanRule {
    fun check(item: ManualUtilizationPlan): MutableList<String>
}

data class ManualUtilizationPlanVisibleGuardRule(var limit: Double) : ManualUtilizationPlanRule {
    override fun check(item: ManualUtilizationPlan): MutableList<String> {
        if (item.visible && item.floorValue > limit) {
            return mutableListOf<String>("floor value ${item.floorValue} exceeds $limit while visible")
        }
        return mutableListOf<String>()
    }
}

data class ManualUtilizationPlanRequiredCodeRule(var message: String) : ManualUtilizationPlanRule {
    override fun check(item: ManualUtilizationPlan): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ManualUtilizationPlanCostRangeRule(var min: Double, var max: Double) : ManualUtilizationPlanRule {
    override fun check(item: ManualUtilizationPlan): MutableList<String> {
        val value = item.cost
        if (value < min || value > max) {
            return mutableListOf<String>("cost $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class ManualUtilizationPlanTimezoneLengthRule(var maxLength: Int) : ManualUtilizationPlanRule {
    override fun check(item: ManualUtilizationPlan): MutableList<String> {
        val size = item.timezone.length
        if (size > maxLength) {
            return mutableListOf<String>("timezone is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a manual utilization plan. */
data class ManualUtilizationPlanValidator(var rules: MutableList<ManualUtilizationPlanRule>) {
    fun validate(item: ManualUtilizationPlan): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ManualUtilizationPlan) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ManualUtilizationPlan>): MutableMap<String, MutableList<String>> {
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

fun defaultManualUtilizationPlanValidator(): ManualUtilizationPlanValidator {
    return ManualUtilizationPlanValidator(
        mutableListOf<ManualUtilizationPlanRule>(
            ManualUtilizationPlanVisibleGuardRule(0.2),
            ManualUtilizationPlanRequiredCodeRule("manual utilization plan needs a value"),
            ManualUtilizationPlanCostRangeRule(5.1, 467.4),
            ManualUtilizationPlanTimezoneLengthRule(32),
        ),
    )
}
