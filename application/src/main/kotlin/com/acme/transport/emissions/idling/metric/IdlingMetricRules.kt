package com.acme.transport.emissions.idling.metric

import com.acme.transport.kit.text.normalize

/** A rule a idling metric must pass. Check returns its problems, or none. */
interface IdlingMetricRule {
    fun check(item: IdlingMetric): MutableList<String>
}

data class IdlingMetricRequiredCodeRule(var message: String) : IdlingMetricRule {
    override fun check(item: IdlingMetric): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class IdlingMetricScoreRangeRule(var min: Double, var max: Double) : IdlingMetricRule {
    override fun check(item: IdlingMetric): MutableList<String> {
        val value = item.score
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("score $value is below $min")
        }
        if (value > max) {
            problems.add("score $value is above $max")
        }
        return problems
    }
}

data class IdlingMetricRequiredTitleRule(var message: String) : IdlingMetricRule {
    override fun check(item: IdlingMetric): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class IdlingMetricRequiredOwnerIdRule(var message: String) : IdlingMetricRule {
    override fun check(item: IdlingMetric): MutableList<String> {
        if (item.ownerId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class IdlingMetricTitleLengthRule(var maxLength: Int) : IdlingMetricRule {
    override fun check(item: IdlingMetric): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class IdlingMetricAllowedRegionRule(var allowed: MutableList<String>) : IdlingMetricRule {
    override fun check(item: IdlingMetric): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.region))) {
            return mutableListOf<String>("region ${item.region} is stale")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a idling metric. */
data class IdlingMetricValidator(var rules: MutableList<IdlingMetricRule>) {
    fun validate(item: IdlingMetric): MutableList<String> {
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

    fun requireValid(item: IdlingMetric) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<IdlingMetric>): MutableMap<String, MutableList<String>> {
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

fun defaultIdlingMetricValidator(): IdlingMetricValidator {
    return IdlingMetricValidator(
        mutableListOf<IdlingMetricRule>(
            IdlingMetricRequiredCodeRule("idling metric needs an entry"),
            IdlingMetricScoreRangeRule(1.5, 1574.5),
            IdlingMetricRequiredTitleRule("idling metric needs this field"),
            IdlingMetricRequiredOwnerIdRule("idling metric needs an entry"),
            IdlingMetricTitleLengthRule(255),
            IdlingMetricAllowedRegionRule(mutableListOf<String>("strict", "weighted", "tiered")),
        ),
    )
}
