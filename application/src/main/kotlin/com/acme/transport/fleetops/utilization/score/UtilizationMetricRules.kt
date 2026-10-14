package com.acme.transport.fleetops.utilization.score

/** A rule a utilization metric must pass. Check returns its problems, or none. */
interface UtilizationMetricRule {
    fun check(item: UtilizationMetric): MutableList<String>
}

data class UtilizationMetricStopCountRangeRule(var min: Int, var max: Int) : UtilizationMetricRule {
    override fun check(item: UtilizationMetric): MutableList<String> {
        val value = item.stopCount
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("stop count $value is below $min")
        }
        if (value > max) {
            problems.add("stop count $value is above $max")
        }
        return problems
    }
}

data class UtilizationMetricNameLengthRule(var maxLength: Int) : UtilizationMetricRule {
    override fun check(item: UtilizationMetric): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class UtilizationMetricRequiredNameRule(var message: String) : UtilizationMetricRule {
    override fun check(item: UtilizationMetric): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class UtilizationMetricRequiredCodeRule(var message: String) : UtilizationMetricRule {
    override fun check(item: UtilizationMetric): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class UtilizationMetricBudgetRangeRule(var min: Double, var max: Double) : UtilizationMetricRule {
    override fun check(item: UtilizationMetric): MutableList<String> {
        val value = item.budget
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("budget $value is below $min")
        }
        if (value > max) {
            problems.add("budget $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a utilization metric. */
data class UtilizationMetricValidator(var rules: MutableList<UtilizationMetricRule>) {
    fun validate(item: UtilizationMetric): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: UtilizationMetric) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<UtilizationMetric>): MutableMap<String, MutableList<String>> {
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

fun defaultUtilizationMetricValidator(): UtilizationMetricValidator {
    return UtilizationMetricValidator(
        mutableListOf<UtilizationMetricRule>(
            UtilizationMetricStopCountRangeRule(64, 86400),
            UtilizationMetricNameLengthRule(1000),
            UtilizationMetricRequiredNameRule("utilization metric needs a value"),
            UtilizationMetricRequiredCodeRule("utilization metric needs an entry"),
            UtilizationMetricBudgetRangeRule(8.7, 4135.1),
        ),
    )
}
