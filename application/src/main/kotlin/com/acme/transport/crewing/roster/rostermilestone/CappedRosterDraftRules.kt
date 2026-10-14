package com.acme.transport.crewing.roster.rostermilestone

/** A rule a capped roster draft must pass. Check returns its problems, or none. */
interface CappedRosterDraftRule {
    fun check(item: CappedRosterDraft): MutableList<String>
}

data class CappedRosterDraftActiveGuardRule(var limit: Double) : CappedRosterDraftRule {
    override fun check(item: CappedRosterDraft): MutableList<String> {
        if (item.active && item.ratio > limit) {
            return mutableListOf<String>("ratio ${item.ratio} exceeds $limit while active")
        }
        return mutableListOf<String>()
    }
}

data class CappedRosterDraftCodePrefixRule(var prefix: String) : CappedRosterDraftRule {
    override fun check(item: CappedRosterDraft): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class CappedRosterDraftLabelLengthRule(var maxLength: Int) : CappedRosterDraftRule {
    override fun check(item: CappedRosterDraft): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CappedRosterDraftCodeLengthRule(var maxLength: Int) : CappedRosterDraftRule {
    override fun check(item: CappedRosterDraft): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CappedRosterDraftRequiredCodeRule(var message: String) : CappedRosterDraftRule {
    override fun check(item: CappedRosterDraft): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a capped roster draft. */
data class CappedRosterDraftValidator(var rules: MutableList<CappedRosterDraftRule>) {
    fun validate(item: CappedRosterDraft): MutableList<String> {
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

    fun requireValid(item: CappedRosterDraft) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CappedRosterDraft>): MutableMap<String, MutableList<String>> {
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

fun defaultCappedRosterDraftValidator(): CappedRosterDraftValidator {
    return CappedRosterDraftValidator(
        mutableListOf<CappedRosterDraftRule>(
            CappedRosterDraftActiveGuardRule(1.2),
            CappedRosterDraftCodePrefixRule("ACM"),
            CappedRosterDraftLabelLengthRule(1000),
            CappedRosterDraftCodeLengthRule(86400),
            CappedRosterDraftRequiredCodeRule("capped roster draft needs this field"),
        ),
    )
}
