package com.acme.transport.emissions.idling.snapshot

import com.acme.transport.kit.text.normalize

/** A rule a adaptive idling segment must pass. Check returns its problems, or none. */
interface AdaptiveIdlingSegmentRule {
    fun check(item: AdaptiveIdlingSegment): MutableList<String>
}

data class AdaptiveIdlingSegmentAllowedLaneRule(var allowed: MutableList<String>) : AdaptiveIdlingSegmentRule {
    override fun check(item: AdaptiveIdlingSegment): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.lane))) {
            return mutableListOf<String>("lane ${item.lane} is inconsistent")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveIdlingSegmentLabelLengthRule(var maxLength: Int) : AdaptiveIdlingSegmentRule {
    override fun check(item: AdaptiveIdlingSegment): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveIdlingSegmentLimitRangeRule(var min: Int, var max: Int) : AdaptiveIdlingSegmentRule {
    override fun check(item: AdaptiveIdlingSegment): MutableList<String> {
        val value = item.limit
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("limit $value is below $min")
        }
        if (value > max) {
            problems.add("limit $value is above $max")
        }
        return problems
    }
}

data class AdaptiveIdlingSegmentCodeLengthRule(var maxLength: Int) : AdaptiveIdlingSegmentRule {
    override fun check(item: AdaptiveIdlingSegment): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveIdlingSegmentRequiredCategoryRule(var message: String) : AdaptiveIdlingSegmentRule {
    override fun check(item: AdaptiveIdlingSegment): MutableList<String> {
        if (item.category.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveIdlingSegmentRequiredCodeRule(var message: String) : AdaptiveIdlingSegmentRule {
    override fun check(item: AdaptiveIdlingSegment): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a adaptive idling segment. */
data class AdaptiveIdlingSegmentValidator(var rules: MutableList<AdaptiveIdlingSegmentRule>) {
    fun validate(item: AdaptiveIdlingSegment): MutableList<String> {
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

    fun requireValid(item: AdaptiveIdlingSegment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AdaptiveIdlingSegment>): MutableMap<String, MutableList<String>> {
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

fun defaultAdaptiveIdlingSegmentValidator(): AdaptiveIdlingSegmentValidator {
    return AdaptiveIdlingSegmentValidator(
        mutableListOf<AdaptiveIdlingSegmentRule>(
            AdaptiveIdlingSegmentAllowedLaneRule(mutableListOf<String>("rolling")),
            AdaptiveIdlingSegmentLabelLengthRule(16),
            AdaptiveIdlingSegmentLimitRangeRule(32, 80),
            AdaptiveIdlingSegmentCodeLengthRule(32),
            AdaptiveIdlingSegmentRequiredCategoryRule("adaptive idling segment needs this field"),
            AdaptiveIdlingSegmentRequiredCodeRule("adaptive idling segment needs an entry"),
        ),
    )
}
