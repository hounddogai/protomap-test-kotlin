package com.acme.transport.linehaul.laneplan.template

import com.acme.transport.kit.text.normalize

/** A rule a scheduled lane plan window must pass. Check returns its problems, or none. */
interface ScheduledLanePlanWindowRule {
    fun check(item: ScheduledLanePlanWindow): MutableList<String>
}

data class ScheduledLanePlanWindowStartsAtRangeRule(var min: Int, var max: Int) : ScheduledLanePlanWindowRule {
    override fun check(item: ScheduledLanePlanWindow): MutableList<String> {
        val value = item.startsAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("starts at $value is below $min")
        }
        if (value > max) {
            problems.add("starts at $value is above $max")
        }
        return problems
    }
}

data class ScheduledLanePlanWindowAllowedCodeRule(
    var allowed: MutableList<String>,
) : ScheduledLanePlanWindowRule {
    override fun check(item: ScheduledLanePlanWindow): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is blocked")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledLanePlanWindowOwnerIdLengthRule(var maxLength: Int) : ScheduledLanePlanWindowRule {
    override fun check(item: ScheduledLanePlanWindow): MutableList<String> {
        val size = item.ownerId.length
        if (size > maxLength) {
            return mutableListOf<String>("owner id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledLanePlanWindowRequiredTitleRule(var message: String) : ScheduledLanePlanWindowRule {
    override fun check(item: ScheduledLanePlanWindow): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ScheduledLanePlanWindowDepotLengthRule(var maxLength: Int) : ScheduledLanePlanWindowRule {
    override fun check(item: ScheduledLanePlanWindow): MutableList<String> {
        val size = item.depot.length
        if (size > maxLength) {
            return mutableListOf<String>("depot is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a scheduled lane plan window. */
data class ScheduledLanePlanWindowValidator(var rules: MutableList<ScheduledLanePlanWindowRule>) {
    fun validate(item: ScheduledLanePlanWindow): MutableList<String> {
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

    fun requireValid(item: ScheduledLanePlanWindow) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ScheduledLanePlanWindow>): MutableMap<String, MutableList<String>> {
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

fun defaultScheduledLanePlanWindowValidator(): ScheduledLanePlanWindowValidator {
    return ScheduledLanePlanWindowValidator(
        mutableListOf<ScheduledLanePlanWindowRule>(
            ScheduledLanePlanWindowStartsAtRangeRule(16, 80),
            ScheduledLanePlanWindowAllowedCodeRule(mutableListOf<String>("default", "manual", "rolling")),
            ScheduledLanePlanWindowOwnerIdLengthRule(16),
            ScheduledLanePlanWindowRequiredTitleRule("scheduled lane plan window needs this field"),
            ScheduledLanePlanWindowDepotLengthRule(3600),
        ),
    )
}
