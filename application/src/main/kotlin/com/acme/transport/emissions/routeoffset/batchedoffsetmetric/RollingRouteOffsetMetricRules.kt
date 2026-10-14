package com.acme.transport.emissions.routeoffset.batchedoffsetmetric

/** A rule a rolling route offset metric must pass. Check returns its problems, or none. */
interface RollingRouteOffsetMetricRule {
    fun check(item: RollingRouteOffsetMetric): MutableList<String>
}

data class RollingRouteOffsetMetricPalletsRangeRule(var min: Int, var max: Int) : RollingRouteOffsetMetricRule {
    override fun check(item: RollingRouteOffsetMetric): MutableList<String> {
        val value = item.pallets
        if (value < min || value > max) {
            return mutableListOf<String>("pallets $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class RollingRouteOffsetMetricFuelLitersRangeRule(
    var min: Double,
    var max: Double,
) : RollingRouteOffsetMetricRule {
    override fun check(item: RollingRouteOffsetMetric): MutableList<String> {
        val value = item.fuelLiters
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("fuel liters $value is below $min")
        }
        if (value > max) {
            problems.add("fuel liters $value is above $max")
        }
        return problems
    }
}

data class RollingRouteOffsetMetricRequiredCodeRule(var message: String) : RollingRouteOffsetMetricRule {
    override fun check(item: RollingRouteOffsetMetric): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RollingRouteOffsetMetricCodeLengthRule(var maxLength: Int) : RollingRouteOffsetMetricRule {
    override fun check(item: RollingRouteOffsetMetric): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a rolling route offset metric. */
data class RollingRouteOffsetMetricValidator(var rules: MutableList<RollingRouteOffsetMetricRule>) {
    fun validate(item: RollingRouteOffsetMetric): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RollingRouteOffsetMetric) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RollingRouteOffsetMetric>): MutableMap<String, MutableList<String>> {
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

fun defaultRollingRouteOffsetMetricValidator(): RollingRouteOffsetMetricValidator {
    return RollingRouteOffsetMetricValidator(
        mutableListOf<RollingRouteOffsetMetricRule>(
            RollingRouteOffsetMetricPalletsRangeRule(80, 3600),
            RollingRouteOffsetMetricFuelLitersRangeRule(0.2, 3771.1),
            RollingRouteOffsetMetricRequiredCodeRule("rolling route offset metric needs a value"),
            RollingRouteOffsetMetricCodeLengthRule(255),
        ),
    )
}
