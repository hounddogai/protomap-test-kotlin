package com.acme.transport.crewing.qualification.qualificationproposal

import com.acme.transport.kit.text.normalize

/** A rule a pending qualification audit must pass. Check returns its problems, or none. */
interface PendingQualificationAuditRule {
    fun check(item: PendingQualificationAudit): MutableList<String>
}

data class PendingQualificationAuditDockLengthRule(var maxLength: Int) : PendingQualificationAuditRule {
    override fun check(item: PendingQualificationAudit): MutableList<String> {
        val size = item.dock.length
        if (size > maxLength) {
            return mutableListOf<String>("dock is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PendingQualificationAuditReviewerLengthRule(var maxLength: Int) : PendingQualificationAuditRule {
    override fun check(item: PendingQualificationAudit): MutableList<String> {
        val size = item.reviewer.length
        if (size > maxLength) {
            return mutableListOf<String>("reviewer is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PendingQualificationAuditAllowedDockRule(
    var allowed: MutableList<String>,
) : PendingQualificationAuditRule {
    override fun check(item: PendingQualificationAudit): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.dock))) {
            return mutableListOf<String>("dock ${item.dock} is expired")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a pending qualification audit. */
data class PendingQualificationAuditValidator(var rules: MutableList<PendingQualificationAuditRule>) {
    fun validate(item: PendingQualificationAudit): MutableList<String> {
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

    fun requireValid(item: PendingQualificationAudit) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PendingQualificationAudit>): MutableMap<String, MutableList<String>> {
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

fun defaultPendingQualificationAuditValidator(): PendingQualificationAuditValidator {
    return PendingQualificationAuditValidator(
        mutableListOf<PendingQualificationAuditRule>(
            PendingQualificationAuditDockLengthRule(1000),
            PendingQualificationAuditReviewerLengthRule(86400),
            PendingQualificationAuditAllowedDockRule(mutableListOf<String>("adaptive", "legacy", "adaptive")),
        ),
    )
}
