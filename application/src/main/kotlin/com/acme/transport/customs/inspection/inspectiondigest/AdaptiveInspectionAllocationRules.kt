package com.acme.transport.customs.inspection.inspectiondigest

import com.acme.transport.kit.text.normalize

/** A rule a adaptive inspection allocation must pass. Check returns its problems, or none. */
interface AdaptiveInspectionAllocationRule {
    fun check(item: AdaptiveInspectionAllocation): MutableList<String>
}

data class AdaptiveInspectionAllocationEtaMinutesRangeRule(
    var min: Int,
    var max: Int,
) : AdaptiveInspectionAllocationRule {
    override fun check(item: AdaptiveInspectionAllocation): MutableList<String> {
        val value = item.etaMinutes
        if (value < min || value > max) {
            return mutableListOf<String>("eta minutes $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveInspectionAllocationCodeLengthRule(var maxLength: Int) : AdaptiveInspectionAllocationRule {
    override fun check(item: AdaptiveInspectionAllocation): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveInspectionAllocationAllowedNameRule(
    var allowed: MutableList<String>,
) : AdaptiveInspectionAllocationRule {
    override fun check(item: AdaptiveInspectionAllocation): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.name))) {
            return mutableListOf<String>("name ${item.name} is inconsistent")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveInspectionAllocationQuantityRangeRule(
    var min: Int,
    var max: Int,
) : AdaptiveInspectionAllocationRule {
    override fun check(item: AdaptiveInspectionAllocation): MutableList<String> {
        val value = item.quantity
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("quantity $value is below $min")
        }
        if (value > max) {
            problems.add("quantity $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a adaptive inspection allocation. */
data class AdaptiveInspectionAllocationValidator(var rules: MutableList<AdaptiveInspectionAllocationRule>) {
    fun validate(item: AdaptiveInspectionAllocation): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: AdaptiveInspectionAllocation) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AdaptiveInspectionAllocation>): MutableMap<String, MutableList<String>> {
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

fun defaultAdaptiveInspectionAllocationValidator(): AdaptiveInspectionAllocationValidator {
    return AdaptiveInspectionAllocationValidator(
        mutableListOf<AdaptiveInspectionAllocationRule>(
            AdaptiveInspectionAllocationEtaMinutesRangeRule(3600, 86400),
            AdaptiveInspectionAllocationCodeLengthRule(120),
            AdaptiveInspectionAllocationAllowedNameRule(mutableListOf<String>("capped")),
            AdaptiveInspectionAllocationQuantityRangeRule(32, 1000),
        ),
    )
}
