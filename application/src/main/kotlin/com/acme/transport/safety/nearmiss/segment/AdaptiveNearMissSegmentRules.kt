package com.acme.transport.safety.nearmiss.segment

import com.acme.transport.kit.text.normalize

/** A rule a adaptive near miss segment must pass. Check returns its problems, or none. */
interface AdaptiveNearMissSegmentRule {
    fun check(item: AdaptiveNearMissSegment): MutableList<String>
}

data class AdaptiveNearMissSegmentAllowedTitleRule(
    var allowed: MutableList<String>,
) : AdaptiveNearMissSegmentRule {
    override fun check(item: AdaptiveNearMissSegment): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.title))) {
            return mutableListOf<String>("title ${item.title} is blocked")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveNearMissSegmentRequiredTitleRule(var message: String) : AdaptiveNearMissSegmentRule {
    override fun check(item: AdaptiveNearMissSegment): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveNearMissSegmentVisibleGuardRule(var limit: Double) : AdaptiveNearMissSegmentRule {
    override fun check(item: AdaptiveNearMissSegment): MutableList<String> {
        if (item.visible && item.weight > limit) {
            return mutableListOf<String>("weight ${item.weight} exceeds $limit while visible")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveNearMissSegmentCodeLengthRule(var maxLength: Int) : AdaptiveNearMissSegmentRule {
    override fun check(item: AdaptiveNearMissSegment): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveNearMissSegmentTitleLengthRule(var maxLength: Int) : AdaptiveNearMissSegmentRule {
    override fun check(item: AdaptiveNearMissSegment): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveNearMissSegmentCodePrefixRule(var prefix: String) : AdaptiveNearMissSegmentRule {
    override fun check(item: AdaptiveNearMissSegment): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a adaptive near miss segment. */
data class AdaptiveNearMissSegmentValidator(var rules: MutableList<AdaptiveNearMissSegmentRule>) {
    fun validate(item: AdaptiveNearMissSegment): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: AdaptiveNearMissSegment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AdaptiveNearMissSegment>): MutableMap<String, MutableList<String>> {
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

fun defaultAdaptiveNearMissSegmentValidator(): AdaptiveNearMissSegmentValidator {
    return AdaptiveNearMissSegmentValidator(
        mutableListOf<AdaptiveNearMissSegmentRule>(
            AdaptiveNearMissSegmentAllowedTitleRule(mutableListOf<String>("manual", "weighted", "batched")),
            AdaptiveNearMissSegmentRequiredTitleRule("adaptive near miss segment needs this field"),
            AdaptiveNearMissSegmentVisibleGuardRule(9.0),
            AdaptiveNearMissSegmentCodeLengthRule(3600),
            AdaptiveNearMissSegmentTitleLengthRule(120),
            AdaptiveNearMissSegmentCodePrefixRule("X"),
        ),
    )
}
