package com.acme.transport.fleetops.serviceplan.request

/** A rule a service plan request must pass. Check returns its problems, or none. */
interface ServicePlanRequestRule {
    fun check(item: ServicePlanRequest): MutableList<String>
}

data class ServicePlanRequestMinimumRangeRule(var min: Double, var max: Double) : ServicePlanRequestRule {
    override fun check(item: ServicePlanRequest): MutableList<String> {
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

data class ServicePlanRequestRequiredCodeRule(var message: String) : ServicePlanRequestRule {
    override fun check(item: ServicePlanRequest): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ServicePlanRequestRequiredDescriptionRule(var message: String) : ServicePlanRequestRule {
    override fun check(item: ServicePlanRequest): MutableList<String> {
        if (item.description.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ServicePlanRequestOdometerRangeRule(var min: Int, var max: Int) : ServicePlanRequestRule {
    override fun check(item: ServicePlanRequest): MutableList<String> {
        val value = item.odometer
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("odometer $value is below $min")
        }
        if (value > max) {
            problems.add("odometer $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a service plan request. */
data class ServicePlanRequestValidator(var rules: MutableList<ServicePlanRequestRule>) {
    fun validate(item: ServicePlanRequest): MutableList<String> {
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

    fun requireValid(item: ServicePlanRequest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ServicePlanRequest>): MutableMap<String, MutableList<String>> {
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

fun defaultServicePlanRequestValidator(): ServicePlanRequestValidator {
    return ServicePlanRequestValidator(
        mutableListOf<ServicePlanRequestRule>(
            ServicePlanRequestMinimumRangeRule(2.4, 3394.0),
            ServicePlanRequestRequiredCodeRule("service plan request needs an entry"),
            ServicePlanRequestRequiredDescriptionRule("service plan request needs a value"),
            ServicePlanRequestOdometerRangeRule(32, 255),
        ),
    )
}
