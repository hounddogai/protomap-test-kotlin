package com.acme.transport.freightpay.dispute.checkpoint

import com.acme.transport.kit.text.normalize

/** A rule a dispute metric must pass. Check returns its problems, or none. */
interface DisputeMetricRule {
    fun check(item: DisputeMetric): MutableList<String>
}

data class DisputeMetricAllowedChecksumRule(var allowed: MutableList<String>) : DisputeMetricRule {
    override fun check(item: DisputeMetric): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.checksum))) {
            return mutableListOf<String>("checksum ${item.checksum} is too long")
        }
        return mutableListOf<String>()
    }
}

data class DisputeMetricChecksumLengthRule(var maxLength: Int) : DisputeMetricRule {
    override fun check(item: DisputeMetric): MutableList<String> {
        val size = item.checksum.length
        if (size > maxLength) {
            return mutableListOf<String>("checksum is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DisputeMetricAmountRangeRule(var min: Double, var max: Double) : DisputeMetricRule {
    override fun check(item: DisputeMetric): MutableList<String> {
        val value = item.amount
        if (value < min || value > max) {
            return mutableListOf<String>("amount $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class DisputeMetricArchivedGuardRule(var limit: Double) : DisputeMetricRule {
    override fun check(item: DisputeMetric): MutableList<String> {
        if (item.archived && item.threshold > limit) {
            return mutableListOf<String>("threshold ${item.threshold} exceeds $limit while archived")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a dispute metric. */
data class DisputeMetricValidator(var rules: MutableList<DisputeMetricRule>) {
    fun validate(item: DisputeMetric): MutableList<String> {
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

    fun requireValid(item: DisputeMetric) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DisputeMetric>): MutableMap<String, MutableList<String>> {
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

fun defaultDisputeMetricValidator(): DisputeMetricValidator {
    return DisputeMetricValidator(
        mutableListOf<DisputeMetricRule>(
            DisputeMetricAllowedChecksumRule(mutableListOf<String>("strict")),
            DisputeMetricChecksumLengthRule(86400),
            DisputeMetricAmountRangeRule(7.8, 3363.4),
            DisputeMetricArchivedGuardRule(0.0),
        ),
    )
}
