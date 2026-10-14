package com.acme.transport.network.hub.allocation

/** A rule a weighted hub plan must pass. Check returns its problems, or none. */
interface WeightedHubPlanRule {
    fun check(item: WeightedHubPlan): MutableList<String>
}

data class WeightedHubPlanCodePrefixRule(var prefix: String) : WeightedHubPlanRule {
    override fun check(item: WeightedHubPlan): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class WeightedHubPlanRequiredDescriptionRule(var message: String) : WeightedHubPlanRule {
    override fun check(item: WeightedHubPlan): MutableList<String> {
        if (item.description.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class WeightedHubPlanWeightRangeRule(var min: Double, var max: Double) : WeightedHubPlanRule {
    override fun check(item: WeightedHubPlan): MutableList<String> {
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

data class WeightedHubPlanRequiredLabelRule(var message: String) : WeightedHubPlanRule {
    override fun check(item: WeightedHubPlan): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class WeightedHubPlanRatioRangeRule(var min: Double, var max: Double) : WeightedHubPlanRule {
    override fun check(item: WeightedHubPlan): MutableList<String> {
        val value = item.ratio
        if (value < min || value > max) {
            return mutableListOf<String>("ratio $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class WeightedHubPlanCodeLengthRule(var maxLength: Int) : WeightedHubPlanRule {
    override fun check(item: WeightedHubPlan): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a weighted hub plan. */
data class WeightedHubPlanValidator(var rules: MutableList<WeightedHubPlanRule>) {
    fun validate(item: WeightedHubPlan): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: WeightedHubPlan) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<WeightedHubPlan>): MutableMap<String, MutableList<String>> {
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

fun defaultWeightedHubPlanValidator(): WeightedHubPlanValidator {
    return WeightedHubPlanValidator(
        mutableListOf<WeightedHubPlanRule>(
            WeightedHubPlanCodePrefixRule("X"),
            WeightedHubPlanRequiredDescriptionRule("weighted hub plan needs an entry"),
            WeightedHubPlanWeightRangeRule(0.9, 3945.0),
            WeightedHubPlanRequiredLabelRule("weighted hub plan needs a value"),
            WeightedHubPlanRatioRangeRule(8.5, 3392.8),
            WeightedHubPlanCodeLengthRule(1000),
        ),
    )
}
