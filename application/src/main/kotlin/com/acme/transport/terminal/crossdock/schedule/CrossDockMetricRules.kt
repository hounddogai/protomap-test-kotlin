package com.acme.transport.terminal.crossdock.schedule

/** A rule a cross dock metric must pass. Check returns its problems, or none. */
interface CrossDockMetricRule {
    fun check(item: CrossDockMetric): MutableList<String>
}

data class CrossDockMetricStartsAtRangeRule(var min: Int, var max: Int) : CrossDockMetricRule {
    override fun check(item: CrossDockMetric): MutableList<String> {
        val value = item.startsAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("starts at $value is below $min")
        }
        if (value > max) {
            problems.add("starts at $value is above $max")
        }
        return problems
    }
}

data class CrossDockMetricOdometerRangeRule(var min: Int, var max: Int) : CrossDockMetricRule {
    override fun check(item: CrossDockMetric): MutableList<String> {
        val value = item.odometer
        if (value < min || value > max) {
            return mutableListOf<String>("odometer $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class CrossDockMetricRequiredLabelRule(var message: String) : CrossDockMetricRule {
    override fun check(item: CrossDockMetric): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a cross dock metric. */
data class CrossDockMetricValidator(var rules: MutableList<CrossDockMetricRule>) {
    fun validate(item: CrossDockMetric): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: CrossDockMetric) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CrossDockMetric>): MutableMap<String, MutableList<String>> {
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

fun defaultCrossDockMetricValidator(): CrossDockMetricValidator {
    return CrossDockMetricValidator(
        mutableListOf<CrossDockMetricRule>(
            CrossDockMetricStartsAtRangeRule(32, 120),
            CrossDockMetricOdometerRangeRule(3600, 86400),
            CrossDockMetricRequiredLabelRule("cross dock metric needs this field"),
        ),
    )
}
