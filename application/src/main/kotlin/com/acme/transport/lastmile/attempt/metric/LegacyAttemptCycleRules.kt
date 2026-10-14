package com.acme.transport.lastmile.attempt.metric

import com.acme.transport.kit.text.normalize

/** A rule a legacy attempt cycle must pass. Check returns its problems, or none. */
interface LegacyAttemptCycleRule {
    fun check(item: LegacyAttemptCycle): MutableList<String>
}

data class LegacyAttemptCycleAllowedTitleRule(var allowed: MutableList<String>) : LegacyAttemptCycleRule {
    override fun check(item: LegacyAttemptCycle): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.title))) {
            return mutableListOf<String>("title ${item.title} is above maximum")
        }
        return mutableListOf<String>()
    }
}

data class LegacyAttemptCycleAttemptsRangeRule(var min: Int, var max: Int) : LegacyAttemptCycleRule {
    override fun check(item: LegacyAttemptCycle): MutableList<String> {
        val value = item.attempts
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("attempts $value is below $min")
        }
        if (value > max) {
            problems.add("attempts $value is above $max")
        }
        return problems
    }
}

data class LegacyAttemptCycleChannelLengthRule(var maxLength: Int) : LegacyAttemptCycleRule {
    override fun check(item: LegacyAttemptCycle): MutableList<String> {
        val size = item.channel.length
        if (size > maxLength) {
            return mutableListOf<String>("channel is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a legacy attempt cycle. */
data class LegacyAttemptCycleValidator(var rules: MutableList<LegacyAttemptCycleRule>) {
    fun validate(item: LegacyAttemptCycle): MutableList<String> {
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

    fun requireValid(item: LegacyAttemptCycle) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LegacyAttemptCycle>): MutableMap<String, MutableList<String>> {
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

fun defaultLegacyAttemptCycleValidator(): LegacyAttemptCycleValidator {
    return LegacyAttemptCycleValidator(
        mutableListOf<LegacyAttemptCycleRule>(
            LegacyAttemptCycleAllowedTitleRule(mutableListOf<String>("strict", "fallback", "lenient")),
            LegacyAttemptCycleAttemptsRangeRule(32, 86400),
            LegacyAttemptCycleChannelLengthRule(32),
        ),
    )
}
