package com.acme.transport.lastmile.locker.claim

import com.acme.transport.kit.text.normalize

/** A rule a locker draft must pass. Check returns its problems, or none. */
interface LockerDraftRule {
    fun check(item: LockerDraft): MutableList<String>
}

data class LockerDraftCodeLengthRule(var maxLength: Int) : LockerDraftRule {
    override fun check(item: LockerDraft): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LockerDraftRequiredCodeRule(var message: String) : LockerDraftRule {
    override fun check(item: LockerDraft): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LockerDraftAllowedLabelRule(var allowed: MutableList<String>) : LockerDraftRule {
    override fun check(item: LockerDraft): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.label))) {
            return mutableListOf<String>("label ${item.label} is expired")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a locker draft. */
data class LockerDraftValidator(var rules: MutableList<LockerDraftRule>) {
    fun validate(item: LockerDraft): MutableList<String> {
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

    fun requireValid(item: LockerDraft) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LockerDraft>): MutableMap<String, MutableList<String>> {
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

fun defaultLockerDraftValidator(): LockerDraftValidator {
    return LockerDraftValidator(
        mutableListOf<LockerDraftRule>(
            LockerDraftCodeLengthRule(80),
            LockerDraftRequiredCodeRule("locker draft needs this field"),
            LockerDraftAllowedLabelRule(mutableListOf<String>()),
        ),
    )
}
