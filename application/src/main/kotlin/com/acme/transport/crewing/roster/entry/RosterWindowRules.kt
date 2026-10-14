package com.acme.transport.crewing.roster.entry

import com.acme.transport.kit.text.normalize

/** A rule a roster window must pass. Check returns its problems, or none. */
interface RosterWindowRule {
    fun check(item: RosterWindow): MutableList<String>
}

data class RosterWindowAllowedCodeRule(var allowed: MutableList<String>) : RosterWindowRule {
    override fun check(item: RosterWindow): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is duplicated")
        }
        return mutableListOf<String>()
    }
}

data class RosterWindowCodePrefixRule(var prefix: String) : RosterWindowRule {
    override fun check(item: RosterWindow): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class RosterWindowRequiredLocaleRule(var message: String) : RosterWindowRule {
    override fun check(item: RosterWindow): MutableList<String> {
        if (item.locale.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a roster window. */
data class RosterWindowValidator(var rules: MutableList<RosterWindowRule>) {
    fun validate(item: RosterWindow): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RosterWindow) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RosterWindow>): MutableMap<String, MutableList<String>> {
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

fun defaultRosterWindowValidator(): RosterWindowValidator {
    return RosterWindowValidator(
        mutableListOf<RosterWindowRule>(
            RosterWindowAllowedCodeRule(mutableListOf<String>("pending", "scheduled")),
            RosterWindowCodePrefixRule("OPS"),
            RosterWindowRequiredLocaleRule("roster window needs this field"),
        ),
    )
}
