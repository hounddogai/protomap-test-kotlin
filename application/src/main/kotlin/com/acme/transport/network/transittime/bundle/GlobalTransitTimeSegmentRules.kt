package com.acme.transport.network.transittime.bundle

import com.acme.transport.kit.text.normalize

/** A rule a global transit time segment must pass. Check returns its problems, or none. */
interface GlobalTransitTimeSegmentRule {
    fun check(item: GlobalTransitTimeSegment): MutableList<String>
}

data class GlobalTransitTimeSegmentAllowedLabelRule(
    var allowed: MutableList<String>,
) : GlobalTransitTimeSegmentRule {
    override fun check(item: GlobalTransitTimeSegment): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.label))) {
            return mutableListOf<String>("label ${item.label} is not allowed")
        }
        return mutableListOf<String>()
    }
}

data class GlobalTransitTimeSegmentThresholdRangeRule(
    var min: Double,
    var max: Double,
) : GlobalTransitTimeSegmentRule {
    override fun check(item: GlobalTransitTimeSegment): MutableList<String> {
        val value = item.threshold
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("threshold $value is below $min")
        }
        if (value > max) {
            problems.add("threshold $value is above $max")
        }
        return problems
    }
}

data class GlobalTransitTimeSegmentCodeLengthRule(var maxLength: Int) : GlobalTransitTimeSegmentRule {
    override fun check(item: GlobalTransitTimeSegment): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class GlobalTransitTimeSegmentRetriesRangeRule(var min: Int, var max: Int) : GlobalTransitTimeSegmentRule {
    override fun check(item: GlobalTransitTimeSegment): MutableList<String> {
        val value = item.retries
        if (value < min || value > max) {
            return mutableListOf<String>("retries $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a global transit time segment. */
data class GlobalTransitTimeSegmentValidator(var rules: MutableList<GlobalTransitTimeSegmentRule>) {
    fun validate(item: GlobalTransitTimeSegment): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: GlobalTransitTimeSegment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<GlobalTransitTimeSegment>): MutableMap<String, MutableList<String>> {
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

fun defaultGlobalTransitTimeSegmentValidator(): GlobalTransitTimeSegmentValidator {
    return GlobalTransitTimeSegmentValidator(
        mutableListOf<GlobalTransitTimeSegmentRule>(
            GlobalTransitTimeSegmentAllowedLabelRule(mutableListOf<String>()),
            GlobalTransitTimeSegmentThresholdRangeRule(9.7, 2244.8),
            GlobalTransitTimeSegmentCodeLengthRule(255),
            GlobalTransitTimeSegmentRetriesRangeRule(80, 3600),
        ),
    )
}
