package com.acme.transport.linehaul.backhaul.backhaulallocation

import com.acme.transport.kit.text.normalize

/** A rule a adaptive backhaul milestone must pass. Check returns its problems, or none. */
interface AdaptiveBackhaulMilestoneRule {
    fun check(item: AdaptiveBackhaulMilestone): MutableList<String>
}

data class AdaptiveBackhaulMilestoneAllowedCodeRule(
    var allowed: MutableList<String>,
) : AdaptiveBackhaulMilestoneRule {
    override fun check(item: AdaptiveBackhaulMilestone): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is not allowed")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveBackhaulMilestoneCategoryLengthRule(var maxLength: Int) : AdaptiveBackhaulMilestoneRule {
    override fun check(item: AdaptiveBackhaulMilestone): MutableList<String> {
        val size = item.category.length
        if (size > maxLength) {
            return mutableListOf<String>("category is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveBackhaulMilestoneThresholdRangeRule(
    var min: Double,
    var max: Double,
) : AdaptiveBackhaulMilestoneRule {
    override fun check(item: AdaptiveBackhaulMilestone): MutableList<String> {
        val value = item.threshold
        if (value < min || value > max) {
            return mutableListOf<String>("threshold $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveBackhaulMilestoneRequiredCategoryRule(var message: String) : AdaptiveBackhaulMilestoneRule {
    override fun check(item: AdaptiveBackhaulMilestone): MutableList<String> {
        if (item.category.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveBackhaulMilestoneCodeLengthRule(var maxLength: Int) : AdaptiveBackhaulMilestoneRule {
    override fun check(item: AdaptiveBackhaulMilestone): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveBackhaulMilestoneRequiredCodeRule(var message: String) : AdaptiveBackhaulMilestoneRule {
    override fun check(item: AdaptiveBackhaulMilestone): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a adaptive backhaul milestone. */
data class AdaptiveBackhaulMilestoneValidator(var rules: MutableList<AdaptiveBackhaulMilestoneRule>) {
    fun validate(item: AdaptiveBackhaulMilestone): MutableList<String> {
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

    fun requireValid(item: AdaptiveBackhaulMilestone) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AdaptiveBackhaulMilestone>): MutableMap<String, MutableList<String>> {
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

fun defaultAdaptiveBackhaulMilestoneValidator(): AdaptiveBackhaulMilestoneValidator {
    return AdaptiveBackhaulMilestoneValidator(
        mutableListOf<AdaptiveBackhaulMilestoneRule>(
            AdaptiveBackhaulMilestoneAllowedCodeRule(mutableListOf<String>()),
            AdaptiveBackhaulMilestoneCategoryLengthRule(86400),
            AdaptiveBackhaulMilestoneThresholdRangeRule(7.0, 3389.1),
            AdaptiveBackhaulMilestoneRequiredCategoryRule("adaptive backhaul milestone needs this field"),
            AdaptiveBackhaulMilestoneCodeLengthRule(3600),
            AdaptiveBackhaulMilestoneRequiredCodeRule("adaptive backhaul milestone needs an entry"),
        ),
    )
}
