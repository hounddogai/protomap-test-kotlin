package com.acme.transport.linehaul.trailer.cycle

import com.acme.transport.kit.text.normalize

/** A rule a scheduled trailer metric must pass. Check returns its problems, or none. */
interface ScheduledTrailerMetricRule {
    fun check(item: ScheduledTrailerMetric): MutableList<String>
}

data class ScheduledTrailerMetricAllowedCodeRule(
    var allowed: MutableList<String>,
) : ScheduledTrailerMetricRule {
    override fun check(item: ScheduledTrailerMetric): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is blocked")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledTrailerMetricCodeLengthRule(var maxLength: Int) : ScheduledTrailerMetricRule {
    override fun check(item: ScheduledTrailerMetric): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledTrailerMetricPriorityRangeRule(var min: Int, var max: Int) : ScheduledTrailerMetricRule {
    override fun check(item: ScheduledTrailerMetric): MutableList<String> {
        val value = item.priority
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("priority $value is below $min")
        }
        if (value > max) {
            problems.add("priority $value is above $max")
        }
        return problems
    }
}

data class ScheduledTrailerMetricRequiredSourceRule(var message: String) : ScheduledTrailerMetricRule {
    override fun check(item: ScheduledTrailerMetric): MutableList<String> {
        if (item.source.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ScheduledTrailerMetricDiscountRangeRule(
    var min: Double,
    var max: Double,
) : ScheduledTrailerMetricRule {
    override fun check(item: ScheduledTrailerMetric): MutableList<String> {
        val value = item.discount
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("discount $value is below $min")
        }
        if (value > max) {
            problems.add("discount $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a scheduled trailer metric. */
data class ScheduledTrailerMetricValidator(var rules: MutableList<ScheduledTrailerMetricRule>) {
    fun validate(item: ScheduledTrailerMetric): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ScheduledTrailerMetric) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ScheduledTrailerMetric>): MutableMap<String, MutableList<String>> {
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

fun defaultScheduledTrailerMetricValidator(): ScheduledTrailerMetricValidator {
    return ScheduledTrailerMetricValidator(
        mutableListOf<ScheduledTrailerMetricRule>(
            ScheduledTrailerMetricAllowedCodeRule(mutableListOf<String>()),
            ScheduledTrailerMetricCodeLengthRule(1000),
            ScheduledTrailerMetricPriorityRangeRule(32, 255),
            ScheduledTrailerMetricRequiredSourceRule("scheduled trailer metric needs a value"),
            ScheduledTrailerMetricDiscountRangeRule(4.9, 4764.4),
        ),
    )
}
