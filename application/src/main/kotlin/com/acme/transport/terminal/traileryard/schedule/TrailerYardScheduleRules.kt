package com.acme.transport.terminal.traileryard.schedule

import com.acme.transport.kit.text.normalize

/** A rule a trailer yard schedule must pass. Check returns its problems, or none. */
interface TrailerYardScheduleRule {
    fun check(item: TrailerYardSchedule): MutableList<String>
}

data class TrailerYardScheduleRequiredCodeRule(var message: String) : TrailerYardScheduleRule {
    override fun check(item: TrailerYardSchedule): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TrailerYardScheduleAllowedCodeRule(var allowed: MutableList<String>) : TrailerYardScheduleRule {
    override fun check(item: TrailerYardSchedule): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is duplicated")
        }
        return mutableListOf<String>()
    }
}

data class TrailerYardScheduleCodePrefixRule(var prefix: String) : TrailerYardScheduleRule {
    override fun check(item: TrailerYardSchedule): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a trailer yard schedule. */
data class TrailerYardScheduleValidator(var rules: MutableList<TrailerYardScheduleRule>) {
    fun validate(item: TrailerYardSchedule): MutableList<String> {
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

    fun requireValid(item: TrailerYardSchedule) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TrailerYardSchedule>): MutableMap<String, MutableList<String>> {
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

fun defaultTrailerYardScheduleValidator(): TrailerYardScheduleValidator {
    return TrailerYardScheduleValidator(
        mutableListOf<TrailerYardScheduleRule>(
            TrailerYardScheduleRequiredCodeRule("trailer yard schedule needs an entry"),
            TrailerYardScheduleAllowedCodeRule(mutableListOf<String>("primary", "pending")),
            TrailerYardScheduleCodePrefixRule("Q"),
        ),
    )
}
