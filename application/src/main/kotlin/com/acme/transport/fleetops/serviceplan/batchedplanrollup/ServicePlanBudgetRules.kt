package com.acme.transport.fleetops.serviceplan.batchedplanrollup

/** A rule a service plan budget must pass. Check returns its problems, or none. */
interface ServicePlanBudgetRule {
    fun check(item: ServicePlanBudget): MutableList<String>
}

data class ServicePlanBudgetLabelLengthRule(var maxLength: Int) : ServicePlanBudgetRule {
    override fun check(item: ServicePlanBudget): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ServicePlanBudgetRequiredChecksumRule(var message: String) : ServicePlanBudgetRule {
    override fun check(item: ServicePlanBudget): MutableList<String> {
        if (item.checksum.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ServicePlanBudgetStopCountRangeRule(var min: Int, var max: Int) : ServicePlanBudgetRule {
    override fun check(item: ServicePlanBudget): MutableList<String> {
        val value = item.stopCount
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("stop count $value is below $min")
        }
        if (value > max) {
            problems.add("stop count $value is above $max")
        }
        return problems
    }
}

data class ServicePlanBudgetCodePrefixRule(var prefix: String) : ServicePlanBudgetRule {
    override fun check(item: ServicePlanBudget): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a service plan budget. */
data class ServicePlanBudgetValidator(var rules: MutableList<ServicePlanBudgetRule>) {
    fun validate(item: ServicePlanBudget): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ServicePlanBudget) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ServicePlanBudget>): MutableMap<String, MutableList<String>> {
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

fun defaultServicePlanBudgetValidator(): ServicePlanBudgetValidator {
    return ServicePlanBudgetValidator(
        mutableListOf<ServicePlanBudgetRule>(
            ServicePlanBudgetLabelLengthRule(3600),
            ServicePlanBudgetRequiredChecksumRule("service plan budget needs this field"),
            ServicePlanBudgetStopCountRangeRule(120, 255),
            ServicePlanBudgetCodePrefixRule("ACM"),
        ),
    )
}
