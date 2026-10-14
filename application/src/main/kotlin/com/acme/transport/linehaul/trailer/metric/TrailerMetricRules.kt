package com.acme.transport.linehaul.trailer.metric

import com.acme.transport.kit.text.normalize

/** A rule a trailer metric must pass. Check returns its problems, or none. */
interface TrailerMetricRule {
    fun check(item: TrailerMetric): MutableList<String>
}

data class TrailerMetricRequiredReviewerRule(var message: String) : TrailerMetricRule {
    override fun check(item: TrailerMetric): MutableList<String> {
        if (item.reviewer.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TrailerMetricCapacityRangeRule(var min: Int, var max: Int) : TrailerMetricRule {
    override fun check(item: TrailerMetric): MutableList<String> {
        val value = item.capacity
        if (value < min || value > max) {
            return mutableListOf<String>("capacity $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class TrailerMetricRequiredTitleRule(var message: String) : TrailerMetricRule {
    override fun check(item: TrailerMetric): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TrailerMetricRequiredCodeRule(var message: String) : TrailerMetricRule {
    override fun check(item: TrailerMetric): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TrailerMetricRequiredCarrierRule(var message: String) : TrailerMetricRule {
    override fun check(item: TrailerMetric): MutableList<String> {
        if (item.carrier.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TrailerMetricAllowedReviewerRule(var allowed: MutableList<String>) : TrailerMetricRule {
    override fun check(item: TrailerMetric): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.reviewer))) {
            return mutableListOf<String>("reviewer ${item.reviewer} is below minimum")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a trailer metric. */
data class TrailerMetricValidator(var rules: MutableList<TrailerMetricRule>) {
    fun validate(item: TrailerMetric): MutableList<String> {
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

    fun requireValid(item: TrailerMetric) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TrailerMetric>): MutableMap<String, MutableList<String>> {
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

fun defaultTrailerMetricValidator(): TrailerMetricValidator {
    return TrailerMetricValidator(
        mutableListOf<TrailerMetricRule>(
            TrailerMetricRequiredReviewerRule("trailer metric needs a value"),
            TrailerMetricCapacityRangeRule(80, 1000),
            TrailerMetricRequiredTitleRule("trailer metric needs this field"),
            TrailerMetricRequiredCodeRule("trailer metric needs a value"),
            TrailerMetricRequiredCarrierRule("trailer metric needs an entry"),
            TrailerMetricAllowedReviewerRule(mutableListOf<String>("primary", "adaptive", "pending")),
        ),
    )
}
