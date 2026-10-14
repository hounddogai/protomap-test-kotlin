package com.acme.transport.freightpay.audit.budget

import com.acme.transport.kit.text.normalize

/** A rule a audit draft must pass. Check returns its problems, or none. */
interface AuditDraftRule {
    fun check(item: AuditDraft): MutableList<String>
}

data class AuditDraftAttemptsRangeRule(var min: Int, var max: Int) : AuditDraftRule {
    override fun check(item: AuditDraft): MutableList<String> {
        val value = item.attempts
        if (value < min || value > max) {
            return mutableListOf<String>("attempts $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class AuditDraftDurationMinutesRangeRule(var min: Int, var max: Int) : AuditDraftRule {
    override fun check(item: AuditDraft): MutableList<String> {
        val value = item.durationMinutes
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("duration minutes $value is below $min")
        }
        if (value > max) {
            problems.add("duration minutes $value is above $max")
        }
        return problems
    }
}

data class AuditDraftAllowedNameRule(var allowed: MutableList<String>) : AuditDraftRule {
    override fun check(item: AuditDraft): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.name))) {
            return mutableListOf<String>("name ${item.name} is above maximum")
        }
        return mutableListOf<String>()
    }
}

data class AuditDraftCodeLengthRule(var maxLength: Int) : AuditDraftRule {
    override fun check(item: AuditDraft): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a audit draft. */
data class AuditDraftValidator(var rules: MutableList<AuditDraftRule>) {
    fun validate(item: AuditDraft): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: AuditDraft) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AuditDraft>): MutableMap<String, MutableList<String>> {
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

fun defaultAuditDraftValidator(): AuditDraftValidator {
    return AuditDraftValidator(
        mutableListOf<AuditDraftRule>(
            AuditDraftAttemptsRangeRule(64, 120),
            AuditDraftDurationMinutesRangeRule(16, 255),
            AuditDraftAllowedNameRule(mutableListOf<String>()),
            AuditDraftCodeLengthRule(3600),
        ),
    )
}
