package com.acme.transport.network.transittime.preference

import com.acme.transport.kit.text.normalize

/** A rule a scheduled transit time digest must pass. Check returns its problems, or none. */
interface ScheduledTransitTimeDigestRule {
    fun check(item: ScheduledTransitTimeDigest): MutableList<String>
}

data class ScheduledTransitTimeDigestVisibleGuardRule(var limit: Int) : ScheduledTransitTimeDigestRule {
    override fun check(item: ScheduledTransitTimeDigest): MutableList<String> {
        if (item.visible && item.stopCount > limit) {
            return mutableListOf<String>("stop count ${item.stopCount} exceeds $limit while visible")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledTransitTimeDigestCodeLengthRule(var maxLength: Int) : ScheduledTransitTimeDigestRule {
    override fun check(item: ScheduledTransitTimeDigest): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledTransitTimeDigestRequiredTitleRule(var message: String) : ScheduledTransitTimeDigestRule {
    override fun check(item: ScheduledTransitTimeDigest): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ScheduledTransitTimeDigestStopCountRangeRule(
    var min: Int,
    var max: Int,
) : ScheduledTransitTimeDigestRule {
    override fun check(item: ScheduledTransitTimeDigest): MutableList<String> {
        val value = item.stopCount
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("stop count $value is below $min")
        }
        if (value > max) {
            problems.add("stop count $value is above $max")
        }
        return problems
    }
}

data class ScheduledTransitTimeDigestAllowedCodeRule(
    var allowed: MutableList<String>,
) : ScheduledTransitTimeDigestRule {
    override fun check(item: ScheduledTransitTimeDigest): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is too long")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a scheduled transit time digest. */
data class ScheduledTransitTimeDigestValidator(var rules: MutableList<ScheduledTransitTimeDigestRule>) {
    fun validate(item: ScheduledTransitTimeDigest): MutableList<String> {
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

    fun requireValid(item: ScheduledTransitTimeDigest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ScheduledTransitTimeDigest>): MutableMap<String, MutableList<String>> {
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

fun defaultScheduledTransitTimeDigestValidator(): ScheduledTransitTimeDigestValidator {
    return ScheduledTransitTimeDigestValidator(
        mutableListOf<ScheduledTransitTimeDigestRule>(
            ScheduledTransitTimeDigestVisibleGuardRule(86400),
            ScheduledTransitTimeDigestCodeLengthRule(86400),
            ScheduledTransitTimeDigestRequiredTitleRule("scheduled transit time digest needs an entry"),
            ScheduledTransitTimeDigestStopCountRangeRule(80, 80),
            ScheduledTransitTimeDigestAllowedCodeRule(mutableListOf<String>("lenient", "adaptive", "primary")),
        ),
    )
}
