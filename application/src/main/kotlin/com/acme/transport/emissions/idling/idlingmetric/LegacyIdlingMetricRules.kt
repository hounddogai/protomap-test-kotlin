package com.acme.transport.emissions.idling.idlingmetric

/** A rule a legacy idling metric must pass. Check returns its problems, or none. */
interface LegacyIdlingMetricRule {
    fun check(item: LegacyIdlingMetric): MutableList<String>
}

data class LegacyIdlingMetricRequiredLabelRule(var message: String) : LegacyIdlingMetricRule {
    override fun check(item: LegacyIdlingMetric): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LegacyIdlingMetricRequiredDescriptionRule(var message: String) : LegacyIdlingMetricRule {
    override fun check(item: LegacyIdlingMetric): MutableList<String> {
        if (item.description.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LegacyIdlingMetricLabelLengthRule(var maxLength: Int) : LegacyIdlingMetricRule {
    override fun check(item: LegacyIdlingMetric): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a legacy idling metric. */
data class LegacyIdlingMetricValidator(var rules: MutableList<LegacyIdlingMetricRule>) {
    fun validate(item: LegacyIdlingMetric): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: LegacyIdlingMetric) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LegacyIdlingMetric>): MutableMap<String, MutableList<String>> {
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

fun defaultLegacyIdlingMetricValidator(): LegacyIdlingMetricValidator {
    return LegacyIdlingMetricValidator(
        mutableListOf<LegacyIdlingMetricRule>(
            LegacyIdlingMetricRequiredLabelRule("legacy idling metric needs an entry"),
            LegacyIdlingMetricRequiredDescriptionRule("legacy idling metric needs this field"),
            LegacyIdlingMetricLabelLengthRule(1000),
        ),
    )
}
