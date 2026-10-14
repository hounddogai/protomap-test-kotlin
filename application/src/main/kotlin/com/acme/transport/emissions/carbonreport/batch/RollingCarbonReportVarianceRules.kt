package com.acme.transport.emissions.carbonreport.batch

import com.acme.transport.kit.text.normalize

/** A rule a rolling carbon report variance must pass. Check returns its problems, or none. */
interface RollingCarbonReportVarianceRule {
    fun check(item: RollingCarbonReportVariance): MutableList<String>
}

data class RollingCarbonReportVarianceReviewerLengthRule(var maxLength: Int) : RollingCarbonReportVarianceRule {
    override fun check(item: RollingCarbonReportVariance): MutableList<String> {
        val size = item.reviewer.length
        if (size > maxLength) {
            return mutableListOf<String>("reviewer is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RollingCarbonReportVarianceAllowedLabelRule(
    var allowed: MutableList<String>,
) : RollingCarbonReportVarianceRule {
    override fun check(item: RollingCarbonReportVariance): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.label))) {
            return mutableListOf<String>("label ${item.label} is stale")
        }
        return mutableListOf<String>()
    }
}

data class RollingCarbonReportVarianceSequenceRangeRule(
    var min: Int,
    var max: Int,
) : RollingCarbonReportVarianceRule {
    override fun check(item: RollingCarbonReportVariance): MutableList<String> {
        val value = item.sequence
        if (value < min || value > max) {
            return mutableListOf<String>("sequence $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class RollingCarbonReportVarianceCodeLengthRule(var maxLength: Int) : RollingCarbonReportVarianceRule {
    override fun check(item: RollingCarbonReportVariance): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a rolling carbon report variance. */
data class RollingCarbonReportVarianceValidator(var rules: MutableList<RollingCarbonReportVarianceRule>) {
    fun validate(item: RollingCarbonReportVariance): MutableList<String> {
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

    fun requireValid(item: RollingCarbonReportVariance) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RollingCarbonReportVariance>): MutableMap<String, MutableList<String>> {
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

fun defaultRollingCarbonReportVarianceValidator(): RollingCarbonReportVarianceValidator {
    return RollingCarbonReportVarianceValidator(
        mutableListOf<RollingCarbonReportVarianceRule>(
            RollingCarbonReportVarianceReviewerLengthRule(16),
            RollingCarbonReportVarianceAllowedLabelRule(mutableListOf<String>("fallback")),
            RollingCarbonReportVarianceSequenceRangeRule(255, 255),
            RollingCarbonReportVarianceCodeLengthRule(32),
        ),
    )
}
