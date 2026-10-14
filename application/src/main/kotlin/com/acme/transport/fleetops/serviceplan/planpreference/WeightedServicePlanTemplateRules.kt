package com.acme.transport.fleetops.serviceplan.planpreference

/** A rule a weighted service plan template must pass. Check returns its problems, or none. */
interface WeightedServicePlanTemplateRule {
    fun check(item: WeightedServicePlanTemplate): MutableList<String>
}

data class WeightedServicePlanTemplateCodePrefixRule(var prefix: String) : WeightedServicePlanTemplateRule {
    override fun check(item: WeightedServicePlanTemplate): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class WeightedServicePlanTemplateRequiredCodeRule(var message: String) : WeightedServicePlanTemplateRule {
    override fun check(item: WeightedServicePlanTemplate): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class WeightedServicePlanTemplateMinimumRangeRule(
    var min: Double,
    var max: Double,
) : WeightedServicePlanTemplateRule {
    override fun check(item: WeightedServicePlanTemplate): MutableList<String> {
        val value = item.minimum
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("minimum $value is below $min")
        }
        if (value > max) {
            problems.add("minimum $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a weighted service plan template. */
data class WeightedServicePlanTemplateValidator(var rules: MutableList<WeightedServicePlanTemplateRule>) {
    fun validate(item: WeightedServicePlanTemplate): MutableList<String> {
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

    fun requireValid(item: WeightedServicePlanTemplate) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<WeightedServicePlanTemplate>): MutableMap<String, MutableList<String>> {
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

fun defaultWeightedServicePlanTemplateValidator(): WeightedServicePlanTemplateValidator {
    return WeightedServicePlanTemplateValidator(
        mutableListOf<WeightedServicePlanTemplateRule>(
            WeightedServicePlanTemplateCodePrefixRule("X"),
            WeightedServicePlanTemplateRequiredCodeRule("weighted service plan template needs this field"),
            WeightedServicePlanTemplateMinimumRangeRule(4.3, 4588.1),
        ),
    )
}
