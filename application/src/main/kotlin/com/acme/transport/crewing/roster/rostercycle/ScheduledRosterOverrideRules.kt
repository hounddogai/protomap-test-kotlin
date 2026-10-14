package com.acme.transport.crewing.roster.rostercycle

import com.acme.transport.kit.text.normalize

/** A rule a scheduled roster override must pass. Check returns its problems, or none. */
interface ScheduledRosterOverrideRule {
    fun check(item: ScheduledRosterOverride): MutableList<String>
}

data class ScheduledRosterOverrideRevisionRangeRule(var min: Int, var max: Int) : ScheduledRosterOverrideRule {
    override fun check(item: ScheduledRosterOverride): MutableList<String> {
        val value = item.revision
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("revision $value is below $min")
        }
        if (value > max) {
            problems.add("revision $value is above $max")
        }
        return problems
    }
}

data class ScheduledRosterOverrideRequiredCodeRule(var message: String) : ScheduledRosterOverrideRule {
    override fun check(item: ScheduledRosterOverride): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ScheduledRosterOverrideAllowedCodeRule(
    var allowed: MutableList<String>,
) : ScheduledRosterOverrideRule {
    override fun check(item: ScheduledRosterOverride): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is stale")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a scheduled roster override. */
data class ScheduledRosterOverrideValidator(var rules: MutableList<ScheduledRosterOverrideRule>) {
    fun validate(item: ScheduledRosterOverride): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ScheduledRosterOverride) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ScheduledRosterOverride>): MutableMap<String, MutableList<String>> {
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

fun defaultScheduledRosterOverrideValidator(): ScheduledRosterOverrideValidator {
    return ScheduledRosterOverrideValidator(
        mutableListOf<ScheduledRosterOverrideRule>(
            ScheduledRosterOverrideRevisionRangeRule(16, 32),
            ScheduledRosterOverrideRequiredCodeRule("scheduled roster override needs this field"),
            ScheduledRosterOverrideAllowedCodeRule(mutableListOf<String>("manual", "weighted")),
        ),
    )
}
