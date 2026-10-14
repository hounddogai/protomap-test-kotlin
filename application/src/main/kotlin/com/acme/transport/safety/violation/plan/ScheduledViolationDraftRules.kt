package com.acme.transport.safety.violation.plan

/** A rule a scheduled violation draft must pass. Check returns its problems, or none. */
interface ScheduledViolationDraftRule {
    fun check(item: ScheduledViolationDraft): MutableList<String>
}

data class ScheduledViolationDraftRequiredDockRule(var message: String) : ScheduledViolationDraftRule {
    override fun check(item: ScheduledViolationDraft): MutableList<String> {
        if (item.dock.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ScheduledViolationDraftRequiredNameRule(var message: String) : ScheduledViolationDraftRule {
    override fun check(item: ScheduledViolationDraft): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ScheduledViolationDraftDockLengthRule(var maxLength: Int) : ScheduledViolationDraftRule {
    override fun check(item: ScheduledViolationDraft): MutableList<String> {
        val size = item.dock.length
        if (size > maxLength) {
            return mutableListOf<String>("dock is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a scheduled violation draft. */
data class ScheduledViolationDraftValidator(var rules: MutableList<ScheduledViolationDraftRule>) {
    fun validate(item: ScheduledViolationDraft): MutableList<String> {
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

    fun requireValid(item: ScheduledViolationDraft) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ScheduledViolationDraft>): MutableMap<String, MutableList<String>> {
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

fun defaultScheduledViolationDraftValidator(): ScheduledViolationDraftValidator {
    return ScheduledViolationDraftValidator(
        mutableListOf<ScheduledViolationDraftRule>(
            ScheduledViolationDraftRequiredDockRule("scheduled violation draft needs an entry"),
            ScheduledViolationDraftRequiredNameRule("scheduled violation draft needs a value"),
            ScheduledViolationDraftDockLengthRule(86400),
        ),
    )
}
