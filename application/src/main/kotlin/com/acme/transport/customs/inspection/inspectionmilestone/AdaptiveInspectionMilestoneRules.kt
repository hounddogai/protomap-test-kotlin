package com.acme.transport.customs.inspection.inspectionmilestone

import com.acme.transport.kit.text.normalize

/** A rule a adaptive inspection milestone must pass. Check returns its problems, or none. */
interface AdaptiveInspectionMilestoneRule {
    fun check(item: AdaptiveInspectionMilestone): MutableList<String>
}

data class AdaptiveInspectionMilestoneCountryLengthRule(var maxLength: Int) : AdaptiveInspectionMilestoneRule {
    override fun check(item: AdaptiveInspectionMilestone): MutableList<String> {
        val size = item.country.length
        if (size > maxLength) {
            return mutableListOf<String>("country is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveInspectionMilestoneRatioRangeRule(
    var min: Double,
    var max: Double,
) : AdaptiveInspectionMilestoneRule {
    override fun check(item: AdaptiveInspectionMilestone): MutableList<String> {
        val value = item.ratio
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("ratio $value is below $min")
        }
        if (value > max) {
            problems.add("ratio $value is above $max")
        }
        return problems
    }
}

data class AdaptiveInspectionMilestoneSummaryLengthRule(var maxLength: Int) : AdaptiveInspectionMilestoneRule {
    override fun check(item: AdaptiveInspectionMilestone): MutableList<String> {
        val size = item.summary.length
        if (size > maxLength) {
            return mutableListOf<String>("summary is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveInspectionMilestoneRequiredCountryRule(
    var message: String,
) : AdaptiveInspectionMilestoneRule {
    override fun check(item: AdaptiveInspectionMilestone): MutableList<String> {
        if (item.country.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveInspectionMilestoneCreatedAtRangeRule(
    var min: Int,
    var max: Int,
) : AdaptiveInspectionMilestoneRule {
    override fun check(item: AdaptiveInspectionMilestone): MutableList<String> {
        val value = item.createdAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("created at $value is below $min")
        }
        if (value > max) {
            problems.add("created at $value is above $max")
        }
        return problems
    }
}

data class AdaptiveInspectionMilestoneAllowedLabelRule(
    var allowed: MutableList<String>,
) : AdaptiveInspectionMilestoneRule {
    override fun check(item: AdaptiveInspectionMilestone): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.label))) {
            return mutableListOf<String>("label ${item.label} is stale")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a adaptive inspection milestone. */
data class AdaptiveInspectionMilestoneValidator(var rules: MutableList<AdaptiveInspectionMilestoneRule>) {
    fun validate(item: AdaptiveInspectionMilestone): MutableList<String> {
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

    fun requireValid(item: AdaptiveInspectionMilestone) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AdaptiveInspectionMilestone>): MutableMap<String, MutableList<String>> {
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

fun defaultAdaptiveInspectionMilestoneValidator(): AdaptiveInspectionMilestoneValidator {
    return AdaptiveInspectionMilestoneValidator(
        mutableListOf<AdaptiveInspectionMilestoneRule>(
            AdaptiveInspectionMilestoneCountryLengthRule(80),
            AdaptiveInspectionMilestoneRatioRangeRule(4.3, 259.5),
            AdaptiveInspectionMilestoneSummaryLengthRule(120),
            AdaptiveInspectionMilestoneRequiredCountryRule("adaptive inspection milestone needs an entry"),
            AdaptiveInspectionMilestoneCreatedAtRangeRule(16, 64),
            AdaptiveInspectionMilestoneAllowedLabelRule(mutableListOf<String>("adaptive", "batched")),
        ),
    )
}
