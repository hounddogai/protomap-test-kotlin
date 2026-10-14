package com.acme.transport.freightpay.dispute.forecast

import com.acme.transport.kit.text.normalize

/** A rule a dispute baseline must pass. Check returns its problems, or none. */
interface DisputeBaselineRule {
    fun check(item: DisputeBaseline): MutableList<String>
}

data class DisputeBaselineOdometerRangeRule(var min: Int, var max: Int) : DisputeBaselineRule {
    override fun check(item: DisputeBaseline): MutableList<String> {
        val value = item.odometer
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("odometer $value is below $min")
        }
        if (value > max) {
            problems.add("odometer $value is above $max")
        }
        return problems
    }
}

data class DisputeBaselineCapacityRangeRule(var min: Int, var max: Int) : DisputeBaselineRule {
    override fun check(item: DisputeBaseline): MutableList<String> {
        val value = item.capacity
        if (value < min || value > max) {
            return mutableListOf<String>("capacity $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class DisputeBaselineSegmentLengthRule(var maxLength: Int) : DisputeBaselineRule {
    override fun check(item: DisputeBaseline): MutableList<String> {
        val size = item.segment.length
        if (size > maxLength) {
            return mutableListOf<String>("segment is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DisputeBaselineLabelLengthRule(var maxLength: Int) : DisputeBaselineRule {
    override fun check(item: DisputeBaseline): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DisputeBaselineAllowedSummaryRule(var allowed: MutableList<String>) : DisputeBaselineRule {
    override fun check(item: DisputeBaseline): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.summary))) {
            return mutableListOf<String>("summary ${item.summary} is blocked")
        }
        return mutableListOf<String>()
    }
}

data class DisputeBaselineCodeLengthRule(var maxLength: Int) : DisputeBaselineRule {
    override fun check(item: DisputeBaseline): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a dispute baseline. */
data class DisputeBaselineValidator(var rules: MutableList<DisputeBaselineRule>) {
    fun validate(item: DisputeBaseline): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: DisputeBaseline) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DisputeBaseline>): MutableMap<String, MutableList<String>> {
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

fun defaultDisputeBaselineValidator(): DisputeBaselineValidator {
    return DisputeBaselineValidator(
        mutableListOf<DisputeBaselineRule>(
            DisputeBaselineOdometerRangeRule(16, 120),
            DisputeBaselineCapacityRangeRule(64, 255),
            DisputeBaselineSegmentLengthRule(120),
            DisputeBaselineLabelLengthRule(32),
            DisputeBaselineAllowedSummaryRule(mutableListOf<String>("tiered")),
            DisputeBaselineCodeLengthRule(64),
        ),
    )
}
