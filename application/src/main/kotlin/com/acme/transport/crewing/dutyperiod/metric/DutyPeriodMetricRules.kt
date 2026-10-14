package com.acme.transport.crewing.dutyperiod.metric

/** A rule a duty period metric must pass. Check returns its problems, or none. */
interface DutyPeriodMetricRule {
    fun check(item: DutyPeriodMetric): MutableList<String>
}

data class DutyPeriodMetricMaximumRangeRule(var min: Double, var max: Double) : DutyPeriodMetricRule {
    override fun check(item: DutyPeriodMetric): MutableList<String> {
        val value = item.maximum
        if (value < min || value > max) {
            return mutableListOf<String>("maximum $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class DutyPeriodMetricUpdatedAtRangeRule(var min: Int, var max: Int) : DutyPeriodMetricRule {
    override fun check(item: DutyPeriodMetric): MutableList<String> {
        val value = item.updatedAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("updated at $value is below $min")
        }
        if (value > max) {
            problems.add("updated at $value is above $max")
        }
        return problems
    }
}

data class DutyPeriodMetricRequiredLaneRule(var message: String) : DutyPeriodMetricRule {
    override fun check(item: DutyPeriodMetric): MutableList<String> {
        if (item.lane.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a duty period metric. */
data class DutyPeriodMetricValidator(var rules: MutableList<DutyPeriodMetricRule>) {
    fun validate(item: DutyPeriodMetric): MutableList<String> {
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

    fun requireValid(item: DutyPeriodMetric) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DutyPeriodMetric>): MutableMap<String, MutableList<String>> {
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

fun defaultDutyPeriodMetricValidator(): DutyPeriodMetricValidator {
    return DutyPeriodMetricValidator(
        mutableListOf<DutyPeriodMetricRule>(
            DutyPeriodMetricMaximumRangeRule(0.9, 1997.6),
            DutyPeriodMetricUpdatedAtRangeRule(64, 80),
            DutyPeriodMetricRequiredLaneRule("duty period metric needs an entry"),
        ),
    )
}
