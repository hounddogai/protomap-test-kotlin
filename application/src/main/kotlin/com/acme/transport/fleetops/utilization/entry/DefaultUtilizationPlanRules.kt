package com.acme.transport.fleetops.utilization.entry

/** A rule a default utilization plan must pass. Check returns its problems, or none. */
interface DefaultUtilizationPlanRule {
    fun check(item: DefaultUtilizationPlan): MutableList<String>
}

data class DefaultUtilizationPlanScoreRangeRule(var min: Double, var max: Double) : DefaultUtilizationPlanRule {
    override fun check(item: DefaultUtilizationPlan): MutableList<String> {
        val value = item.score
        if (value < min || value > max) {
            return mutableListOf<String>("score $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class DefaultUtilizationPlanCodeLengthRule(var maxLength: Int) : DefaultUtilizationPlanRule {
    override fun check(item: DefaultUtilizationPlan): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DefaultUtilizationPlanRequiredCarrierRule(var message: String) : DefaultUtilizationPlanRule {
    override fun check(item: DefaultUtilizationPlan): MutableList<String> {
        if (item.carrier.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a default utilization plan. */
data class DefaultUtilizationPlanValidator(var rules: MutableList<DefaultUtilizationPlanRule>) {
    fun validate(item: DefaultUtilizationPlan): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: DefaultUtilizationPlan) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DefaultUtilizationPlan>): MutableMap<String, MutableList<String>> {
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

fun defaultDefaultUtilizationPlanValidator(): DefaultUtilizationPlanValidator {
    return DefaultUtilizationPlanValidator(
        mutableListOf<DefaultUtilizationPlanRule>(
            DefaultUtilizationPlanScoreRangeRule(3.8, 1618.1),
            DefaultUtilizationPlanCodeLengthRule(86400),
            DefaultUtilizationPlanRequiredCarrierRule("default utilization plan needs an entry"),
        ),
    )
}
