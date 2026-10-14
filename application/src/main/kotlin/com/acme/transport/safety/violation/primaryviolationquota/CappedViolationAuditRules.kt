package com.acme.transport.safety.violation.primaryviolationquota

import com.acme.transport.kit.text.normalize

/** A rule a capped violation audit must pass. Check returns its problems, or none. */
interface CappedViolationAuditRule {
    fun check(item: CappedViolationAudit): MutableList<String>
}

data class CappedViolationAuditLocaleLengthRule(var maxLength: Int) : CappedViolationAuditRule {
    override fun check(item: CappedViolationAudit): MutableList<String> {
        val size = item.locale.length
        if (size > maxLength) {
            return mutableListOf<String>("locale is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CappedViolationAuditApprovedGuardRule(var limit: Double) : CappedViolationAuditRule {
    override fun check(item: CappedViolationAudit): MutableList<String> {
        if (item.approved && item.score > limit) {
            return mutableListOf<String>("score ${item.score} exceeds $limit while approved")
        }
        return mutableListOf<String>()
    }
}

data class CappedViolationAuditRequiredTitleRule(var message: String) : CappedViolationAuditRule {
    override fun check(item: CappedViolationAudit): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CappedViolationAuditAllowedLocaleRule(var allowed: MutableList<String>) : CappedViolationAuditRule {
    override fun check(item: CappedViolationAudit): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.locale))) {
            return mutableListOf<String>("locale ${item.locale} is stale")
        }
        return mutableListOf<String>()
    }
}

data class CappedViolationAuditRequiredSegmentRule(var message: String) : CappedViolationAuditRule {
    override fun check(item: CappedViolationAudit): MutableList<String> {
        if (item.segment.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a capped violation audit. */
data class CappedViolationAuditValidator(var rules: MutableList<CappedViolationAuditRule>) {
    fun validate(item: CappedViolationAudit): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: CappedViolationAudit) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CappedViolationAudit>): MutableMap<String, MutableList<String>> {
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

fun defaultCappedViolationAuditValidator(): CappedViolationAuditValidator {
    return CappedViolationAuditValidator(
        mutableListOf<CappedViolationAuditRule>(
            CappedViolationAuditLocaleLengthRule(86400),
            CappedViolationAuditApprovedGuardRule(4.7),
            CappedViolationAuditRequiredTitleRule("capped violation audit needs this field"),
            CappedViolationAuditAllowedLocaleRule(mutableListOf<String>("lenient")),
            CappedViolationAuditRequiredSegmentRule("capped violation audit needs an entry"),
        ),
    )
}
