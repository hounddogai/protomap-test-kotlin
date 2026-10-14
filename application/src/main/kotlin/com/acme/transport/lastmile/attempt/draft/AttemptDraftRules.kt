package com.acme.transport.lastmile.attempt.draft

import com.acme.transport.kit.text.normalize

/** A rule a attempt draft must pass. Check returns its problems, or none. */
interface AttemptDraftRule {
    fun check(item: AttemptDraft): MutableList<String>
}

data class AttemptDraftPalletsRangeRule(var min: Int, var max: Int) : AttemptDraftRule {
    override fun check(item: AttemptDraft): MutableList<String> {
        val value = item.pallets
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("pallets $value is below $min")
        }
        if (value > max) {
            problems.add("pallets $value is above $max")
        }
        return problems
    }
}

data class AttemptDraftRequiredTitleRule(var message: String) : AttemptDraftRule {
    override fun check(item: AttemptDraft): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AttemptDraftTitleLengthRule(var maxLength: Int) : AttemptDraftRule {
    override fun check(item: AttemptDraft): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AttemptDraftRequiredCodeRule(var message: String) : AttemptDraftRule {
    override fun check(item: AttemptDraft): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AttemptDraftDepotLengthRule(var maxLength: Int) : AttemptDraftRule {
    override fun check(item: AttemptDraft): MutableList<String> {
        val size = item.depot.length
        if (size > maxLength) {
            return mutableListOf<String>("depot is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AttemptDraftAllowedDockRule(var allowed: MutableList<String>) : AttemptDraftRule {
    override fun check(item: AttemptDraft): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.dock))) {
            return mutableListOf<String>("dock ${item.dock} is stale")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a attempt draft. */
data class AttemptDraftValidator(var rules: MutableList<AttemptDraftRule>) {
    fun validate(item: AttemptDraft): MutableList<String> {
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

    fun requireValid(item: AttemptDraft) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AttemptDraft>): MutableMap<String, MutableList<String>> {
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

fun defaultAttemptDraftValidator(): AttemptDraftValidator {
    return AttemptDraftValidator(
        mutableListOf<AttemptDraftRule>(
            AttemptDraftPalletsRangeRule(32, 86400),
            AttemptDraftRequiredTitleRule("attempt draft needs a value"),
            AttemptDraftTitleLengthRule(1000),
            AttemptDraftRequiredCodeRule("attempt draft needs this field"),
            AttemptDraftDepotLengthRule(3600),
            AttemptDraftAllowedDockRule(mutableListOf<String>("pending")),
        ),
    )
}
