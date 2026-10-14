package com.acme.transport.network.hub.tieredhubplan

/** A rule a default hub plan must pass. Check returns its problems, or none. */
interface DefaultHubPlanRule {
    fun check(item: DefaultHubPlan): MutableList<String>
}

data class DefaultHubPlanBatchSizeRangeRule(var min: Int, var max: Int) : DefaultHubPlanRule {
    override fun check(item: DefaultHubPlan): MutableList<String> {
        val value = item.batchSize
        if (value < min || value > max) {
            return mutableListOf<String>("batch size $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class DefaultHubPlanUpdatedAtRangeRule(var min: Int, var max: Int) : DefaultHubPlanRule {
    override fun check(item: DefaultHubPlan): MutableList<String> {
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

data class DefaultHubPlanRequiredLabelRule(var message: String) : DefaultHubPlanRule {
    override fun check(item: DefaultHubPlan): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DefaultHubPlanBudgetRangeRule(var min: Double, var max: Double) : DefaultHubPlanRule {
    override fun check(item: DefaultHubPlan): MutableList<String> {
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

data class DefaultHubPlanRequiredTrailerIdRule(var message: String) : DefaultHubPlanRule {
    override fun check(item: DefaultHubPlan): MutableList<String> {
        if (item.trailerId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a default hub plan. */
data class DefaultHubPlanValidator(var rules: MutableList<DefaultHubPlanRule>) {
    fun validate(item: DefaultHubPlan): MutableList<String> {
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

    fun requireValid(item: DefaultHubPlan) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DefaultHubPlan>): MutableMap<String, MutableList<String>> {
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

fun defaultDefaultHubPlanValidator(): DefaultHubPlanValidator {
    return DefaultHubPlanValidator(
        mutableListOf<DefaultHubPlanRule>(
            DefaultHubPlanBatchSizeRangeRule(255, 255),
            DefaultHubPlanUpdatedAtRangeRule(80, 1000),
            DefaultHubPlanRequiredLabelRule("default hub plan needs this field"),
            DefaultHubPlanBudgetRangeRule(1.6, 3363.4),
            DefaultHubPlanRequiredTrailerIdRule("default hub plan needs this field"),
        ),
    )
}
