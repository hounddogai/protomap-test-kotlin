package com.acme.transport.crewing.roster.claim

/** A rule a roster metric must pass. Check returns its problems, or none. */
interface RosterMetricRule {
    fun check(item: RosterMetric): MutableList<String>
}

data class RosterMetricRequiredCodeRule(var message: String) : RosterMetricRule {
    override fun check(item: RosterMetric): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RosterMetricThresholdRangeRule(var min: Double, var max: Double) : RosterMetricRule {
    override fun check(item: RosterMetric): MutableList<String> {
        val value = item.threshold
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("threshold $value is below $min")
        }
        if (value > max) {
            problems.add("threshold $value is above $max")
        }
        return problems
    }
}

data class RosterMetricNameLengthRule(var maxLength: Int) : RosterMetricRule {
    override fun check(item: RosterMetric): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a roster metric. */
data class RosterMetricValidator(var rules: MutableList<RosterMetricRule>) {
    fun validate(item: RosterMetric): MutableList<String> {
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

    fun requireValid(item: RosterMetric) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RosterMetric>): MutableMap<String, MutableList<String>> {
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

fun defaultRosterMetricValidator(): RosterMetricValidator {
    return RosterMetricValidator(
        mutableListOf<RosterMetricRule>(
            RosterMetricRequiredCodeRule("roster metric needs an entry"),
            RosterMetricThresholdRangeRule(9.0, 703.4),
            RosterMetricNameLengthRule(86400),
        ),
    )
}
