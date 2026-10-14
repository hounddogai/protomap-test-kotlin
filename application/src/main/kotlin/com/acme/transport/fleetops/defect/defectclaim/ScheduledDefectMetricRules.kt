package com.acme.transport.fleetops.defect.defectclaim

/** A rule a scheduled defect metric must pass. Check returns its problems, or none. */
interface ScheduledDefectMetricRule {
    fun check(item: ScheduledDefectMetric): MutableList<String>
}

data class ScheduledDefectMetricTitleLengthRule(var maxLength: Int) : ScheduledDefectMetricRule {
    override fun check(item: ScheduledDefectMetric): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledDefectMetricFuelLitersRangeRule(
    var min: Double,
    var max: Double,
) : ScheduledDefectMetricRule {
    override fun check(item: ScheduledDefectMetric): MutableList<String> {
        val value = item.fuelLiters
        if (value < min || value > max) {
            return mutableListOf<String>("fuel liters $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledDefectMetricRequiredTitleRule(var message: String) : ScheduledDefectMetricRule {
    override fun check(item: ScheduledDefectMetric): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ScheduledDefectMetricRequiredCodeRule(var message: String) : ScheduledDefectMetricRule {
    override fun check(item: ScheduledDefectMetric): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ScheduledDefectMetricAttemptsRangeRule(var min: Int, var max: Int) : ScheduledDefectMetricRule {
    override fun check(item: ScheduledDefectMetric): MutableList<String> {
        val value = item.attempts
        if (value < min || value > max) {
            return mutableListOf<String>("attempts $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a scheduled defect metric. */
data class ScheduledDefectMetricValidator(var rules: MutableList<ScheduledDefectMetricRule>) {
    fun validate(item: ScheduledDefectMetric): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ScheduledDefectMetric) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ScheduledDefectMetric>): MutableMap<String, MutableList<String>> {
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

fun defaultScheduledDefectMetricValidator(): ScheduledDefectMetricValidator {
    return ScheduledDefectMetricValidator(
        mutableListOf<ScheduledDefectMetricRule>(
            ScheduledDefectMetricTitleLengthRule(120),
            ScheduledDefectMetricFuelLitersRangeRule(1.0, 2679.1),
            ScheduledDefectMetricRequiredTitleRule("scheduled defect metric needs a value"),
            ScheduledDefectMetricRequiredCodeRule("scheduled defect metric needs an entry"),
            ScheduledDefectMetricAttemptsRangeRule(80, 120),
        ),
    )
}
