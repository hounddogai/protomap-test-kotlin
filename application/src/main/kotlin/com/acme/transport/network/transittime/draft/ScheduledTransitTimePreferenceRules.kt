package com.acme.transport.network.transittime.draft

import com.acme.transport.kit.text.normalize

/** A rule a scheduled transit time preference must pass. Check returns its problems, or none. */
interface ScheduledTransitTimePreferenceRule {
    fun check(item: ScheduledTransitTimePreference): MutableList<String>
}

data class ScheduledTransitTimePreferenceArchivedGuardRule(
    var limit: Double,
) : ScheduledTransitTimePreferenceRule {
    override fun check(item: ScheduledTransitTimePreference): MutableList<String> {
        if (item.archived && item.rate > limit) {
            return mutableListOf<String>("rate ${item.rate} exceeds $limit while archived")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledTransitTimePreferenceLimitRangeRule(
    var min: Int,
    var max: Int,
) : ScheduledTransitTimePreferenceRule {
    override fun check(item: ScheduledTransitTimePreference): MutableList<String> {
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

data class ScheduledTransitTimePreferenceAllowedCodeRule(
    var allowed: MutableList<String>,
) : ScheduledTransitTimePreferenceRule {
    override fun check(item: ScheduledTransitTimePreference): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is too long")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a scheduled transit time preference. */
data class ScheduledTransitTimePreferenceValidator(var rules: MutableList<ScheduledTransitTimePreferenceRule>) {
    fun validate(item: ScheduledTransitTimePreference): MutableList<String> {
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

    fun requireValid(item: ScheduledTransitTimePreference) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ScheduledTransitTimePreference>): MutableMap<String, MutableList<String>> {
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

fun defaultScheduledTransitTimePreferenceValidator(): ScheduledTransitTimePreferenceValidator {
    return ScheduledTransitTimePreferenceValidator(
        mutableListOf<ScheduledTransitTimePreferenceRule>(
            ScheduledTransitTimePreferenceArchivedGuardRule(6.1),
            ScheduledTransitTimePreferenceLimitRangeRule(32, 255),
            ScheduledTransitTimePreferenceAllowedCodeRule(mutableListOf<String>("regional", "fallback")),
        ),
    )
}
