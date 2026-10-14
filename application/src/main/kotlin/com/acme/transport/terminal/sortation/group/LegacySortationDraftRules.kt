package com.acme.transport.terminal.sortation.group

import com.acme.transport.kit.text.normalize

/** A rule a legacy sortation draft must pass. Check returns its problems, or none. */
interface LegacySortationDraftRule {
    fun check(item: LegacySortationDraft): MutableList<String>
}

data class LegacySortationDraftLabelLengthRule(var maxLength: Int) : LegacySortationDraftRule {
    override fun check(item: LegacySortationDraft): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LegacySortationDraftRequiredCodeRule(var message: String) : LegacySortationDraftRule {
    override fun check(item: LegacySortationDraft): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LegacySortationDraftCodeLengthRule(var maxLength: Int) : LegacySortationDraftRule {
    override fun check(item: LegacySortationDraft): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LegacySortationDraftDockLengthRule(var maxLength: Int) : LegacySortationDraftRule {
    override fun check(item: LegacySortationDraft): MutableList<String> {
        val size = item.dock.length
        if (size > maxLength) {
            return mutableListOf<String>("dock is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LegacySortationDraftAllowedDockRule(var allowed: MutableList<String>) : LegacySortationDraftRule {
    override fun check(item: LegacySortationDraft): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.dock))) {
            return mutableListOf<String>("dock ${item.dock} is not allowed")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a legacy sortation draft. */
data class LegacySortationDraftValidator(var rules: MutableList<LegacySortationDraftRule>) {
    fun validate(item: LegacySortationDraft): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: LegacySortationDraft) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LegacySortationDraft>): MutableMap<String, MutableList<String>> {
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

fun defaultLegacySortationDraftValidator(): LegacySortationDraftValidator {
    return LegacySortationDraftValidator(
        mutableListOf<LegacySortationDraftRule>(
            LegacySortationDraftLabelLengthRule(32),
            LegacySortationDraftRequiredCodeRule("legacy sortation draft needs a value"),
            LegacySortationDraftCodeLengthRule(86400),
            LegacySortationDraftDockLengthRule(16),
            LegacySortationDraftAllowedDockRule(mutableListOf<String>()),
        ),
    )
}
