package com.acme.transport.safety.coaching.segment

import com.acme.transport.kit.text.normalize

/** A rule a strict coaching schedule must pass. Check returns its problems, or none. */
interface StrictCoachingScheduleRule {
    fun check(item: StrictCoachingSchedule): MutableList<String>
}

data class StrictCoachingScheduleRequiredExternalRefRule(var message: String) : StrictCoachingScheduleRule {
    override fun check(item: StrictCoachingSchedule): MutableList<String> {
        if (item.externalRef.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class StrictCoachingScheduleAllowedExternalRefRule(
    var allowed: MutableList<String>,
) : StrictCoachingScheduleRule {
    override fun check(item: StrictCoachingSchedule): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.externalRef))) {
            return mutableListOf<String>("external ref ${item.externalRef} is stale")
        }
        return mutableListOf<String>()
    }
}

data class StrictCoachingScheduleRequiredCodeRule(var message: String) : StrictCoachingScheduleRule {
    override fun check(item: StrictCoachingSchedule): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class StrictCoachingScheduleRequiredTitleRule(var message: String) : StrictCoachingScheduleRule {
    override fun check(item: StrictCoachingSchedule): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class StrictCoachingScheduleExternalRefLengthRule(var maxLength: Int) : StrictCoachingScheduleRule {
    override fun check(item: StrictCoachingSchedule): MutableList<String> {
        val size = item.externalRef.length
        if (size > maxLength) {
            return mutableListOf<String>("external ref is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a strict coaching schedule. */
data class StrictCoachingScheduleValidator(var rules: MutableList<StrictCoachingScheduleRule>) {
    fun validate(item: StrictCoachingSchedule): MutableList<String> {
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

    fun requireValid(item: StrictCoachingSchedule) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<StrictCoachingSchedule>): MutableMap<String, MutableList<String>> {
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

fun defaultStrictCoachingScheduleValidator(): StrictCoachingScheduleValidator {
    return StrictCoachingScheduleValidator(
        mutableListOf<StrictCoachingScheduleRule>(
            StrictCoachingScheduleRequiredExternalRefRule("strict coaching schedule needs an entry"),
            StrictCoachingScheduleAllowedExternalRefRule(mutableListOf<String>("regional")),
            StrictCoachingScheduleRequiredCodeRule("strict coaching schedule needs this field"),
            StrictCoachingScheduleRequiredTitleRule("strict coaching schedule needs this field"),
            StrictCoachingScheduleExternalRefLengthRule(64),
        ),
    )
}
