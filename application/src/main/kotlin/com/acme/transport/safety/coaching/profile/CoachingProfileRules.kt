package com.acme.transport.safety.coaching.profile

import com.acme.transport.kit.text.normalize

/** A rule a coaching profile must pass. Check returns its problems, or none. */
interface CoachingProfileRule {
    fun check(item: CoachingProfile): MutableList<String>
}

data class CoachingProfileCodePrefixRule(var prefix: String) : CoachingProfileRule {
    override fun check(item: CoachingProfile): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class CoachingProfileRequiredTitleRule(var message: String) : CoachingProfileRule {
    override fun check(item: CoachingProfile): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CoachingProfileAllowedSummaryRule(var allowed: MutableList<String>) : CoachingProfileRule {
    override fun check(item: CoachingProfile): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.summary))) {
            return mutableListOf<String>("summary ${item.summary} is above maximum")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a coaching profile. */
data class CoachingProfileValidator(var rules: MutableList<CoachingProfileRule>) {
    fun validate(item: CoachingProfile): MutableList<String> {
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

    fun requireValid(item: CoachingProfile) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CoachingProfile>): MutableMap<String, MutableList<String>> {
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

fun defaultCoachingProfileValidator(): CoachingProfileValidator {
    return CoachingProfileValidator(
        mutableListOf<CoachingProfileRule>(
            CoachingProfileCodePrefixRule("REF"),
            CoachingProfileRequiredTitleRule("coaching profile needs an entry"),
            CoachingProfileAllowedSummaryRule(mutableListOf<String>()),
        ),
    )
}
