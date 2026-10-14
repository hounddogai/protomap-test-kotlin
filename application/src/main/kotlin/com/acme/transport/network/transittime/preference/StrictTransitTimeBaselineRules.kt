package com.acme.transport.network.transittime.preference

import com.acme.transport.kit.text.normalize

/** A rule a strict transit time baseline must pass. Check returns its problems, or none. */
interface StrictTransitTimeBaselineRule {
    fun check(item: StrictTransitTimeBaseline): MutableList<String>
}

data class StrictTransitTimeBaselineAllowedCodeRule(
    var allowed: MutableList<String>,
) : StrictTransitTimeBaselineRule {
    override fun check(item: StrictTransitTimeBaseline): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is out of range")
        }
        return mutableListOf<String>()
    }
}

data class StrictTransitTimeBaselinePinnedGuardRule(var limit: Int) : StrictTransitTimeBaselineRule {
    override fun check(item: StrictTransitTimeBaseline): MutableList<String> {
        if (item.pinned && item.limit > limit) {
            return mutableListOf<String>("limit ${item.limit} exceeds $limit while pinned")
        }
        return mutableListOf<String>()
    }
}

data class StrictTransitTimeBaselineRequiredCodeRule(var message: String) : StrictTransitTimeBaselineRule {
    override fun check(item: StrictTransitTimeBaseline): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class StrictTransitTimeBaselineRequiredChannelRule(var message: String) : StrictTransitTimeBaselineRule {
    override fun check(item: StrictTransitTimeBaseline): MutableList<String> {
        if (item.channel.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class StrictTransitTimeBaselineLabelLengthRule(var maxLength: Int) : StrictTransitTimeBaselineRule {
    override fun check(item: StrictTransitTimeBaseline): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a strict transit time baseline. */
data class StrictTransitTimeBaselineValidator(var rules: MutableList<StrictTransitTimeBaselineRule>) {
    fun validate(item: StrictTransitTimeBaseline): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: StrictTransitTimeBaseline) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<StrictTransitTimeBaseline>): MutableMap<String, MutableList<String>> {
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

fun defaultStrictTransitTimeBaselineValidator(): StrictTransitTimeBaselineValidator {
    return StrictTransitTimeBaselineValidator(
        mutableListOf<StrictTransitTimeBaselineRule>(
            StrictTransitTimeBaselineAllowedCodeRule(mutableListOf<String>("adaptive", "tiered")),
            StrictTransitTimeBaselinePinnedGuardRule(120),
            StrictTransitTimeBaselineRequiredCodeRule("strict transit time baseline needs an entry"),
            StrictTransitTimeBaselineRequiredChannelRule("strict transit time baseline needs an entry"),
            StrictTransitTimeBaselineLabelLengthRule(120),
        ),
    )
}
