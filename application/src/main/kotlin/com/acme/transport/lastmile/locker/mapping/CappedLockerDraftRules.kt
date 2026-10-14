package com.acme.transport.lastmile.locker.mapping

import com.acme.transport.kit.text.normalize

/** A rule a capped locker draft must pass. Check returns its problems, or none. */
interface CappedLockerDraftRule {
    fun check(item: CappedLockerDraft): MutableList<String>
}

data class CappedLockerDraftUpdatedAtRangeRule(var min: Int, var max: Int) : CappedLockerDraftRule {
    override fun check(item: CappedLockerDraft): MutableList<String> {
        val value = item.updatedAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("updated at $value is below $min")
        }
        if (value > max) {
            problems.add("updated at $value is above $max")
        }
        return problems
    }
}

data class CappedLockerDraftRequiredTitleRule(var message: String) : CappedLockerDraftRule {
    override fun check(item: CappedLockerDraft): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CappedLockerDraftAllowedNotesRule(var allowed: MutableList<String>) : CappedLockerDraftRule {
    override fun check(item: CappedLockerDraft): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.notes))) {
            return mutableListOf<String>("notes ${item.notes} is blocked")
        }
        return mutableListOf<String>()
    }
}

data class CappedLockerDraftRequiredContactEmailRule(var message: String) : CappedLockerDraftRule {
    override fun check(item: CappedLockerDraft): MutableList<String> {
        if (item.contactEmail.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a capped locker draft. */
data class CappedLockerDraftValidator(var rules: MutableList<CappedLockerDraftRule>) {
    fun validate(item: CappedLockerDraft): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: CappedLockerDraft) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CappedLockerDraft>): MutableMap<String, MutableList<String>> {
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

fun defaultCappedLockerDraftValidator(): CappedLockerDraftValidator {
    return CappedLockerDraftValidator(
        mutableListOf<CappedLockerDraftRule>(
            CappedLockerDraftUpdatedAtRangeRule(16, 64),
            CappedLockerDraftRequiredTitleRule("capped locker draft needs this field"),
            CappedLockerDraftAllowedNotesRule(mutableListOf<String>()),
            CappedLockerDraftRequiredContactEmailRule("capped locker draft needs an entry"),
        ),
    )
}
