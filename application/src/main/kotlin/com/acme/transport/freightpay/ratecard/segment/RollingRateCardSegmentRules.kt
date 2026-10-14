package com.acme.transport.freightpay.ratecard.segment

import com.acme.transport.kit.text.normalize

/** A rule a rolling rate card segment must pass. Check returns its problems, or none. */
interface RollingRateCardSegmentRule {
    fun check(item: RollingRateCardSegment): MutableList<String>
}

data class RollingRateCardSegmentThresholdRangeRule(
    var min: Double,
    var max: Double,
) : RollingRateCardSegmentRule {
    override fun check(item: RollingRateCardSegment): MutableList<String> {
        val value = item.threshold
        if (value < min || value > max) {
            return mutableListOf<String>("threshold $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class RollingRateCardSegmentPinnedGuardRule(var limit: Double) : RollingRateCardSegmentRule {
    override fun check(item: RollingRateCardSegment): MutableList<String> {
        if (item.pinned && item.distanceKm > limit) {
            return mutableListOf<String>("distance km ${item.distanceKm} exceeds $limit while pinned")
        }
        return mutableListOf<String>()
    }
}

data class RollingRateCardSegmentAllowedCodeRule(
    var allowed: MutableList<String>,
) : RollingRateCardSegmentRule {
    override fun check(item: RollingRateCardSegment): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is missing")
        }
        return mutableListOf<String>()
    }
}

data class RollingRateCardSegmentRequiredCodeRule(var message: String) : RollingRateCardSegmentRule {
    override fun check(item: RollingRateCardSegment): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a rolling rate card segment. */
data class RollingRateCardSegmentValidator(var rules: MutableList<RollingRateCardSegmentRule>) {
    fun validate(item: RollingRateCardSegment): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RollingRateCardSegment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RollingRateCardSegment>): MutableMap<String, MutableList<String>> {
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

fun defaultRollingRateCardSegmentValidator(): RollingRateCardSegmentValidator {
    return RollingRateCardSegmentValidator(
        mutableListOf<RollingRateCardSegmentRule>(
            RollingRateCardSegmentThresholdRangeRule(0.6, 2685.5),
            RollingRateCardSegmentPinnedGuardRule(9.8),
            RollingRateCardSegmentAllowedCodeRule(mutableListOf<String>("lenient", "legacy", "rolling")),
            RollingRateCardSegmentRequiredCodeRule("rolling rate card segment needs this field"),
        ),
    )
}
