package com.acme.transport.fleetops.serviceplan.metric

/** A rule a service plan metric must pass. Check returns its problems, or none. */
interface ServicePlanMetricRule {
    fun check(item: ServicePlanMetric): MutableList<String>
}

data class ServicePlanMetricFuelLitersRangeRule(var min: Double, var max: Double) : ServicePlanMetricRule {
    override fun check(item: ServicePlanMetric): MutableList<String> {
        val value = item.fuelLiters
        if (value < min || value > max) {
            return mutableListOf<String>("fuel liters $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class ServicePlanMetricRequiredLabelRule(var message: String) : ServicePlanMetricRule {
    override fun check(item: ServicePlanMetric): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ServicePlanMetricFloorValueRangeRule(var min: Double, var max: Double) : ServicePlanMetricRule {
    override fun check(item: ServicePlanMetric): MutableList<String> {
        val value = item.floorValue
        if (value < min || value > max) {
            return mutableListOf<String>("floor value $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a service plan metric. */
data class ServicePlanMetricValidator(var rules: MutableList<ServicePlanMetricRule>) {
    fun validate(item: ServicePlanMetric): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ServicePlanMetric) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ServicePlanMetric>): MutableMap<String, MutableList<String>> {
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

fun defaultServicePlanMetricValidator(): ServicePlanMetricValidator {
    return ServicePlanMetricValidator(
        mutableListOf<ServicePlanMetricRule>(
            ServicePlanMetricFuelLitersRangeRule(0.1, 1558.7),
            ServicePlanMetricRequiredLabelRule("service plan metric needs a value"),
            ServicePlanMetricFloorValueRangeRule(0.6, 3932.3),
        ),
    )
}
