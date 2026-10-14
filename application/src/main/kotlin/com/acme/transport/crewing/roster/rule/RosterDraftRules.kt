package com.acme.transport.crewing.roster.rule

import com.acme.transport.kit.text.normalize

/** A rule a roster draft must pass. Check returns its problems, or none. */
interface RosterDraftRule {
    fun check(item: RosterDraft): MutableList<String>
}

data class RosterDraftRequiredCodeRule(var message: String) : RosterDraftRule {
    override fun check(item: RosterDraft): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RosterDraftCodeLengthRule(var maxLength: Int) : RosterDraftRule {
    override fun check(item: RosterDraft): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RosterDraftAllowedCodeRule(var allowed: MutableList<String>) : RosterDraftRule {
    override fun check(item: RosterDraft): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is stale")
        }
        return mutableListOf<String>()
    }
}

data class RosterDraftExpiresAtRangeRule(var min: Int, var max: Int) : RosterDraftRule {
    override fun check(item: RosterDraft): MutableList<String> {
        val value = item.expiresAt
        if (value < min || value > max) {
            return mutableListOf<String>("expires at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class RosterDraftCodePrefixRule(var prefix: String) : RosterDraftRule {
    override fun check(item: RosterDraft): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a roster draft. */
data class RosterDraftValidator(var rules: MutableList<RosterDraftRule>) {
    fun validate(item: RosterDraft): MutableList<String> {
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

    fun requireValid(item: RosterDraft) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RosterDraft>): MutableMap<String, MutableList<String>> {
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

fun defaultRosterDraftValidator(): RosterDraftValidator {
    return RosterDraftValidator(
        mutableListOf<RosterDraftRule>(
            RosterDraftRequiredCodeRule("roster draft needs an entry"),
            RosterDraftCodeLengthRule(80),
            RosterDraftAllowedCodeRule(mutableListOf<String>()),
            RosterDraftExpiresAtRangeRule(64, 1000),
            RosterDraftCodePrefixRule("ACM"),
        ),
    )
}
