package com.acme.transport.fleetops.serviceplan.checkpoint

/** A rule a service plan cycle must pass. Check returns its problems, or none. */
interface ServicePlanCycleRule {
    fun check(item: ServicePlanCycle): MutableList<String>
}

data class ServicePlanCycleCodePrefixRule(var prefix: String) : ServicePlanCycleRule {
    override fun check(item: ServicePlanCycle): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class ServicePlanCycleCodeLengthRule(var maxLength: Int) : ServicePlanCycleRule {
    override fun check(item: ServicePlanCycle): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ServicePlanCycleWindowDaysRangeRule(var min: Int, var max: Int) : ServicePlanCycleRule {
    override fun check(item: ServicePlanCycle): MutableList<String> {
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

data class ServicePlanCycleRequiredReviewerRule(var message: String) : ServicePlanCycleRule {
    override fun check(item: ServicePlanCycle): MutableList<String> {
        if (item.reviewer.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ServicePlanCycleRequiredTitleRule(var message: String) : ServicePlanCycleRule {
    override fun check(item: ServicePlanCycle): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a service plan cycle. */
data class ServicePlanCycleValidator(var rules: MutableList<ServicePlanCycleRule>) {
    fun validate(item: ServicePlanCycle): MutableList<String> {
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

    fun requireValid(item: ServicePlanCycle) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ServicePlanCycle>): MutableMap<String, MutableList<String>> {
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

fun defaultServicePlanCycleValidator(): ServicePlanCycleValidator {
    return ServicePlanCycleValidator(
        mutableListOf<ServicePlanCycleRule>(
            ServicePlanCycleCodePrefixRule("Q"),
            ServicePlanCycleCodeLengthRule(64),
            ServicePlanCycleWindowDaysRangeRule(16, 3600),
            ServicePlanCycleRequiredReviewerRule("service plan cycle needs an entry"),
            ServicePlanCycleRequiredTitleRule("service plan cycle needs a value"),
        ),
    )
}
