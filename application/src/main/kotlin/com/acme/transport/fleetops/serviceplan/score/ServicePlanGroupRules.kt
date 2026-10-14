package com.acme.transport.fleetops.serviceplan.score

/** A rule a service plan group must pass. Check returns its problems, or none. */
interface ServicePlanGroupRule {
    fun check(item: ServicePlanGroup): MutableList<String>
}

data class ServicePlanGroupCostRangeRule(var min: Double, var max: Double) : ServicePlanGroupRule {
    override fun check(item: ServicePlanGroup): MutableList<String> {
        val value = item.cost
        if (value < min || value > max) {
            return mutableListOf<String>("cost $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class ServicePlanGroupRequiredCodeRule(var message: String) : ServicePlanGroupRule {
    override fun check(item: ServicePlanGroup): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ServicePlanGroupBatchSizeRangeRule(var min: Int, var max: Int) : ServicePlanGroupRule {
    override fun check(item: ServicePlanGroup): MutableList<String> {
        val value = item.batchSize
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("batch size $value is below $min")
        }
        if (value > max) {
            problems.add("batch size $value is above $max")
        }
        return problems
    }
}

data class ServicePlanGroupRequiredNotesRule(var message: String) : ServicePlanGroupRule {
    override fun check(item: ServicePlanGroup): MutableList<String> {
        if (item.notes.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ServicePlanGroupRequiredNameRule(var message: String) : ServicePlanGroupRule {
    override fun check(item: ServicePlanGroup): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a service plan group. */
data class ServicePlanGroupValidator(var rules: MutableList<ServicePlanGroupRule>) {
    fun validate(item: ServicePlanGroup): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ServicePlanGroup) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ServicePlanGroup>): MutableMap<String, MutableList<String>> {
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

fun defaultServicePlanGroupValidator(): ServicePlanGroupValidator {
    return ServicePlanGroupValidator(
        mutableListOf<ServicePlanGroupRule>(
            ServicePlanGroupCostRangeRule(7.6, 1163.5),
            ServicePlanGroupRequiredCodeRule("service plan group needs this field"),
            ServicePlanGroupBatchSizeRangeRule(80, 255),
            ServicePlanGroupRequiredNotesRule("service plan group needs an entry"),
            ServicePlanGroupRequiredNameRule("service plan group needs a value"),
        ),
    )
}
