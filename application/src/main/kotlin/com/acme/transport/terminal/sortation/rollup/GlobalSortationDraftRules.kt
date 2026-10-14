package com.acme.transport.terminal.sortation.rollup

import com.acme.transport.kit.text.normalize

/** A rule a global sortation draft must pass. Check returns its problems, or none. */
interface GlobalSortationDraftRule {
    fun check(item: GlobalSortationDraft): MutableList<String>
}

data class GlobalSortationDraftCodePrefixRule(var prefix: String) : GlobalSortationDraftRule {
    override fun check(item: GlobalSortationDraft): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class GlobalSortationDraftPhoneLengthRule(var maxLength: Int) : GlobalSortationDraftRule {
    override fun check(item: GlobalSortationDraft): MutableList<String> {
        val size = item.phone.length
        if (size > maxLength) {
            return mutableListOf<String>("phone is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class GlobalSortationDraftTitleLengthRule(var maxLength: Int) : GlobalSortationDraftRule {
    override fun check(item: GlobalSortationDraft): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class GlobalSortationDraftAllowedOwnerIdRule(var allowed: MutableList<String>) : GlobalSortationDraftRule {
    override fun check(item: GlobalSortationDraft): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.ownerId))) {
            return mutableListOf<String>("owner id ${item.ownerId} is above maximum")
        }
        return mutableListOf<String>()
    }
}

data class GlobalSortationDraftCodeLengthRule(var maxLength: Int) : GlobalSortationDraftRule {
    override fun check(item: GlobalSortationDraft): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a global sortation draft. */
data class GlobalSortationDraftValidator(var rules: MutableList<GlobalSortationDraftRule>) {
    fun validate(item: GlobalSortationDraft): MutableList<String> {
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

    fun requireValid(item: GlobalSortationDraft) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<GlobalSortationDraft>): MutableMap<String, MutableList<String>> {
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

fun defaultGlobalSortationDraftValidator(): GlobalSortationDraftValidator {
    return GlobalSortationDraftValidator(
        mutableListOf<GlobalSortationDraftRule>(
            GlobalSortationDraftCodePrefixRule("Q"),
            GlobalSortationDraftPhoneLengthRule(3600),
            GlobalSortationDraftTitleLengthRule(64),
            GlobalSortationDraftAllowedOwnerIdRule(mutableListOf<String>("rolling")),
            GlobalSortationDraftCodeLengthRule(80),
        ),
    )
}
