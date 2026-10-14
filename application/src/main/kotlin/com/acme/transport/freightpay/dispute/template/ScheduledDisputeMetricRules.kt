package com.acme.transport.freightpay.dispute.template

import com.acme.transport.kit.text.normalize

/** A rule a scheduled dispute metric must pass. Check returns its problems, or none. */
interface ScheduledDisputeMetricRule {
    fun check(item: ScheduledDisputeMetric): MutableList<String>
}

data class ScheduledDisputeMetricNameLengthRule(var maxLength: Int) : ScheduledDisputeMetricRule {
    override fun check(item: ScheduledDisputeMetric): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledDisputeMetricLocaleLengthRule(var maxLength: Int) : ScheduledDisputeMetricRule {
    override fun check(item: ScheduledDisputeMetric): MutableList<String> {
        val size = item.locale.length
        if (size > maxLength) {
            return mutableListOf<String>("locale is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledDisputeMetricAllowedSourceRule(
    var allowed: MutableList<String>,
) : ScheduledDisputeMetricRule {
    override fun check(item: ScheduledDisputeMetric): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.source))) {
            return mutableListOf<String>("source ${item.source} is out of range")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a scheduled dispute metric. */
data class ScheduledDisputeMetricValidator(var rules: MutableList<ScheduledDisputeMetricRule>) {
    fun validate(item: ScheduledDisputeMetric): MutableList<String> {
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

    fun requireValid(item: ScheduledDisputeMetric) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ScheduledDisputeMetric>): MutableMap<String, MutableList<String>> {
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

fun defaultScheduledDisputeMetricValidator(): ScheduledDisputeMetricValidator {
    return ScheduledDisputeMetricValidator(
        mutableListOf<ScheduledDisputeMetricRule>(
            ScheduledDisputeMetricNameLengthRule(120),
            ScheduledDisputeMetricLocaleLengthRule(64),
            ScheduledDisputeMetricAllowedSourceRule(mutableListOf<String>("primary", "global")),
        ),
    )
}
