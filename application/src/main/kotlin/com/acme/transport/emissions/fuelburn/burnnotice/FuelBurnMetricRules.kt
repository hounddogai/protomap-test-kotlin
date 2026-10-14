package com.acme.transport.emissions.fuelburn.burnnotice

/** A rule a fuel burn metric must pass. Check returns its problems, or none. */
interface FuelBurnMetricRule {
    fun check(item: FuelBurnMetric): MutableList<String>
}

data class FuelBurnMetricCodePrefixRule(var prefix: String) : FuelBurnMetricRule {
    override fun check(item: FuelBurnMetric): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class FuelBurnMetricLimitRangeRule(var min: Int, var max: Int) : FuelBurnMetricRule {
    override fun check(item: FuelBurnMetric): MutableList<String> {
        val value = item.limit
        if (value < min || value > max) {
            return mutableListOf<String>("limit $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class FuelBurnMetricRequiredLaneRule(var message: String) : FuelBurnMetricRule {
    override fun check(item: FuelBurnMetric): MutableList<String> {
        if (item.lane.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a fuel burn metric. */
data class FuelBurnMetricValidator(var rules: MutableList<FuelBurnMetricRule>) {
    fun validate(item: FuelBurnMetric): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: FuelBurnMetric) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FuelBurnMetric>): MutableMap<String, MutableList<String>> {
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

fun defaultFuelBurnMetricValidator(): FuelBurnMetricValidator {
    return FuelBurnMetricValidator(
        mutableListOf<FuelBurnMetricRule>(
            FuelBurnMetricCodePrefixRule("OPS"),
            FuelBurnMetricLimitRangeRule(32, 255),
            FuelBurnMetricRequiredLaneRule("fuel burn metric needs a value"),
        ),
    )
}
