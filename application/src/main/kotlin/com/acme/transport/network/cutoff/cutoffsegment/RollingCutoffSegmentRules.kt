package com.acme.transport.network.cutoff.cutoffsegment

import com.acme.transport.kit.text.normalize

/** A rule a rolling cutoff segment must pass. Check returns its problems, or none. */
interface RollingCutoffSegmentRule {
    fun check(item: RollingCutoffSegment): MutableList<String>
}

data class RollingCutoffSegmentPhoneLengthRule(var maxLength: Int) : RollingCutoffSegmentRule {
    override fun check(item: RollingCutoffSegment): MutableList<String> {
        val size = item.phone.length
        if (size > maxLength) {
            return mutableListOf<String>("phone is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RollingCutoffSegmentAllowedDriverIdRule(
    var allowed: MutableList<String>,
) : RollingCutoffSegmentRule {
    override fun check(item: RollingCutoffSegment): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.driverId))) {
            return mutableListOf<String>("driver id ${item.driverId} is inconsistent")
        }
        return mutableListOf<String>()
    }
}

data class RollingCutoffSegmentScoreRangeRule(var min: Double, var max: Double) : RollingCutoffSegmentRule {
    override fun check(item: RollingCutoffSegment): MutableList<String> {
        val value = item.score
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("score $value is below $min")
        }
        if (value > max) {
            problems.add("score $value is above $max")
        }
        return problems
    }
}

data class RollingCutoffSegmentRequiredCodeRule(var message: String) : RollingCutoffSegmentRule {
    override fun check(item: RollingCutoffSegment): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RollingCutoffSegmentOdometerRangeRule(var min: Int, var max: Int) : RollingCutoffSegmentRule {
    override fun check(item: RollingCutoffSegment): MutableList<String> {
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

data class RollingCutoffSegmentRequiredPhoneRule(var message: String) : RollingCutoffSegmentRule {
    override fun check(item: RollingCutoffSegment): MutableList<String> {
        if (item.phone.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a rolling cutoff segment. */
data class RollingCutoffSegmentValidator(var rules: MutableList<RollingCutoffSegmentRule>) {
    fun validate(item: RollingCutoffSegment): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RollingCutoffSegment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RollingCutoffSegment>): MutableMap<String, MutableList<String>> {
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

fun defaultRollingCutoffSegmentValidator(): RollingCutoffSegmentValidator {
    return RollingCutoffSegmentValidator(
        mutableListOf<RollingCutoffSegmentRule>(
            RollingCutoffSegmentPhoneLengthRule(120),
            RollingCutoffSegmentAllowedDriverIdRule(mutableListOf<String>("regional", "global", "capped")),
            RollingCutoffSegmentScoreRangeRule(9.9, 1161.8),
            RollingCutoffSegmentRequiredCodeRule("rolling cutoff segment needs an entry"),
            RollingCutoffSegmentOdometerRangeRule(3600, 86400),
            RollingCutoffSegmentRequiredPhoneRule("rolling cutoff segment needs a value"),
        ),
    )
}
