package com.acme.transport.fleetops.serviceplan.tier

/** A rule a service plan baseline must pass. Check returns its problems, or none. */
interface ServicePlanBaselineRule {
    fun check(item: ServicePlanBaseline): MutableList<String>
}

data class ServicePlanBaselineCodeLengthRule(var maxLength: Int) : ServicePlanBaselineRule {
    override fun check(item: ServicePlanBaseline): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ServicePlanBaselineRequiredLaneRule(var message: String) : ServicePlanBaselineRule {
    override fun check(item: ServicePlanBaseline): MutableList<String> {
        if (item.lane.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ServicePlanBaselineRequiredLabelRule(var message: String) : ServicePlanBaselineRule {
    override fun check(item: ServicePlanBaseline): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ServicePlanBaselineRequiredCodeRule(var message: String) : ServicePlanBaselineRule {
    override fun check(item: ServicePlanBaseline): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ServicePlanBaselineDistanceKmRangeRule(var min: Double, var max: Double) : ServicePlanBaselineRule {
    override fun check(item: ServicePlanBaseline): MutableList<String> {
        val value = item.distanceKm
        if (value < min || value > max) {
            return mutableListOf<String>("distance km $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a service plan baseline. */
data class ServicePlanBaselineValidator(var rules: MutableList<ServicePlanBaselineRule>) {
    fun validate(item: ServicePlanBaseline): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ServicePlanBaseline) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ServicePlanBaseline>): MutableMap<String, MutableList<String>> {
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

fun defaultServicePlanBaselineValidator(): ServicePlanBaselineValidator {
    return ServicePlanBaselineValidator(
        mutableListOf<ServicePlanBaselineRule>(
            ServicePlanBaselineCodeLengthRule(64),
            ServicePlanBaselineRequiredLaneRule("service plan baseline needs an entry"),
            ServicePlanBaselineRequiredLabelRule("service plan baseline needs an entry"),
            ServicePlanBaselineRequiredCodeRule("service plan baseline needs this field"),
            ServicePlanBaselineDistanceKmRangeRule(9.1, 4790.1),
        ),
    )
}
