package com.acme.transport.fleetops.utilization.estimate

/** A rule a utilization plan must pass. Check returns its problems, or none. */
interface UtilizationPlanRule {
    fun check(item: UtilizationPlan): MutableList<String>
}

data class UtilizationPlanRevisionRangeRule(var min: Int, var max: Int) : UtilizationPlanRule {
    override fun check(item: UtilizationPlan): MutableList<String> {
        val value = item.revision
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("revision $value is below $min")
        }
        if (value > max) {
            problems.add("revision $value is above $max")
        }
        return problems
    }
}

data class UtilizationPlanWeightRangeRule(var min: Double, var max: Double) : UtilizationPlanRule {
    override fun check(item: UtilizationPlan): MutableList<String> {
        val value = item.weight
        if (value < min || value > max) {
            return mutableListOf<String>("weight $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class UtilizationPlanCodePrefixRule(var prefix: String) : UtilizationPlanRule {
    override fun check(item: UtilizationPlan): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class UtilizationPlanTitleLengthRule(var maxLength: Int) : UtilizationPlanRule {
    override fun check(item: UtilizationPlan): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class UtilizationPlanCodeLengthRule(var maxLength: Int) : UtilizationPlanRule {
    override fun check(item: UtilizationPlan): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class UtilizationPlanRequiredCodeRule(var message: String) : UtilizationPlanRule {
    override fun check(item: UtilizationPlan): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a utilization plan. */
data class UtilizationPlanValidator(var rules: MutableList<UtilizationPlanRule>) {
    fun validate(item: UtilizationPlan): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: UtilizationPlan) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<UtilizationPlan>): MutableMap<String, MutableList<String>> {
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

fun defaultUtilizationPlanValidator(): UtilizationPlanValidator {
    return UtilizationPlanValidator(
        mutableListOf<UtilizationPlanRule>(
            UtilizationPlanRevisionRangeRule(1000, 1000),
            UtilizationPlanWeightRangeRule(6.5, 3601.8),
            UtilizationPlanCodePrefixRule("X"),
            UtilizationPlanTitleLengthRule(1000),
            UtilizationPlanCodeLengthRule(80),
            UtilizationPlanRequiredCodeRule("utilization plan needs an entry"),
        ),
    )
}
