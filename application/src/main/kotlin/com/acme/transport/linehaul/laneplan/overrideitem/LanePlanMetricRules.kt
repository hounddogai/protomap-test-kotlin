package com.acme.transport.linehaul.laneplan.overrideitem

/** A rule a lane plan metric must pass. Check returns its problems, or none. */
interface LanePlanMetricRule {
    fun check(item: LanePlanMetric): MutableList<String>
}

data class LanePlanMetricLimitRangeRule(var min: Int, var max: Int) : LanePlanMetricRule {
    override fun check(item: LanePlanMetric): MutableList<String> {
        val value = item.limit
        if (value < min || value > max) {
            return mutableListOf<String>("limit $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class LanePlanMetricStartsAtRangeRule(var min: Int, var max: Int) : LanePlanMetricRule {
    override fun check(item: LanePlanMetric): MutableList<String> {
        val value = item.startsAt
        if (value < min || value > max) {
            return mutableListOf<String>("starts at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class LanePlanMetricRequiredCodeRule(var message: String) : LanePlanMetricRule {
    override fun check(item: LanePlanMetric): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a lane plan metric. */
data class LanePlanMetricValidator(var rules: MutableList<LanePlanMetricRule>) {
    fun validate(item: LanePlanMetric): MutableList<String> {
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

    fun requireValid(item: LanePlanMetric) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LanePlanMetric>): MutableMap<String, MutableList<String>> {
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

fun defaultLanePlanMetricValidator(): LanePlanMetricValidator {
    return LanePlanMetricValidator(
        mutableListOf<LanePlanMetricRule>(
            LanePlanMetricLimitRangeRule(32, 120),
            LanePlanMetricStartsAtRangeRule(32, 120),
            LanePlanMetricRequiredCodeRule("lane plan metric needs an entry"),
        ),
    )
}
