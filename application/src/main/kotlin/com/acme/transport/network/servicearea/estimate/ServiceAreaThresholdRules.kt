package com.acme.transport.network.servicearea.estimate

/** A rule a service area threshold must pass. Check returns its problems, or none. */
interface ServiceAreaThresholdRule {
    fun check(item: ServiceAreaThreshold): MutableList<String>
}

data class ServiceAreaThresholdRouteCodeLengthRule(var maxLength: Int) : ServiceAreaThresholdRule {
    override fun check(item: ServiceAreaThreshold): MutableList<String> {
        val size = item.routeCode.length
        if (size > maxLength) {
            return mutableListOf<String>("route code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ServiceAreaThresholdWindowDaysRangeRule(var min: Int, var max: Int) : ServiceAreaThresholdRule {
    override fun check(item: ServiceAreaThreshold): MutableList<String> {
        val value = item.windowDays
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("window days $value is below $min")
        }
        if (value > max) {
            problems.add("window days $value is above $max")
        }
        return problems
    }
}

data class ServiceAreaThresholdLimitRangeRule(var min: Int, var max: Int) : ServiceAreaThresholdRule {
    override fun check(item: ServiceAreaThreshold): MutableList<String> {
        val value = item.limit
        if (value < min || value > max) {
            return mutableListOf<String>("limit $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class ServiceAreaThresholdNameLengthRule(var maxLength: Int) : ServiceAreaThresholdRule {
    override fun check(item: ServiceAreaThreshold): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a service area threshold. */
data class ServiceAreaThresholdValidator(var rules: MutableList<ServiceAreaThresholdRule>) {
    fun validate(item: ServiceAreaThreshold): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ServiceAreaThreshold) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ServiceAreaThreshold>): MutableMap<String, MutableList<String>> {
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

fun defaultServiceAreaThresholdValidator(): ServiceAreaThresholdValidator {
    return ServiceAreaThresholdValidator(
        mutableListOf<ServiceAreaThresholdRule>(
            ServiceAreaThresholdRouteCodeLengthRule(255),
            ServiceAreaThresholdWindowDaysRangeRule(16, 32),
            ServiceAreaThresholdLimitRangeRule(1000, 86400),
            ServiceAreaThresholdNameLengthRule(64),
        ),
    )
}
