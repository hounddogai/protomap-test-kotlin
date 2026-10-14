package com.acme.transport.fleetops.serviceplan.threshold

/** A rule a service plan threshold must pass. Check returns its problems, or none. */
interface ServicePlanThresholdRule {
    fun check(item: ServicePlanThreshold): MutableList<String>
}

data class ServicePlanThresholdRequiredCodeRule(var message: String) : ServicePlanThresholdRule {
    override fun check(item: ServicePlanThreshold): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ServicePlanThresholdRequiredTitleRule(var message: String) : ServicePlanThresholdRule {
    override fun check(item: ServicePlanThreshold): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ServicePlanThresholdCodeLengthRule(var maxLength: Int) : ServicePlanThresholdRule {
    override fun check(item: ServicePlanThreshold): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ServicePlanThresholdRateRangeRule(var min: Double, var max: Double) : ServicePlanThresholdRule {
    override fun check(item: ServicePlanThreshold): MutableList<String> {
        val value = item.rate
        if (value < min || value > max) {
            return mutableListOf<String>("rate $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class ServicePlanThresholdRequiredDriverIdRule(var message: String) : ServicePlanThresholdRule {
    override fun check(item: ServicePlanThreshold): MutableList<String> {
        if (item.driverId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a service plan threshold. */
data class ServicePlanThresholdValidator(var rules: MutableList<ServicePlanThresholdRule>) {
    fun validate(item: ServicePlanThreshold): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ServicePlanThreshold) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ServicePlanThreshold>): MutableMap<String, MutableList<String>> {
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

fun defaultServicePlanThresholdValidator(): ServicePlanThresholdValidator {
    return ServicePlanThresholdValidator(
        mutableListOf<ServicePlanThresholdRule>(
            ServicePlanThresholdRequiredCodeRule("service plan threshold needs a value"),
            ServicePlanThresholdRequiredTitleRule("service plan threshold needs a value"),
            ServicePlanThresholdCodeLengthRule(16),
            ServicePlanThresholdRateRangeRule(3.8, 1835.1),
            ServicePlanThresholdRequiredDriverIdRule("service plan threshold needs this field"),
        ),
    )
}
