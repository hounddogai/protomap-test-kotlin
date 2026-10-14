package com.acme.transport.customs.hold.holdaudit

import com.acme.transport.kit.text.normalize

/** A rule a manual hold audit must pass. Check returns its problems, or none. */
interface ManualHoldAuditRule {
    fun check(item: ManualHoldAudit): MutableList<String>
}

data class ManualHoldAuditNameLengthRule(var maxLength: Int) : ManualHoldAuditRule {
    override fun check(item: ManualHoldAudit): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ManualHoldAuditRequiredCategoryRule(var message: String) : ManualHoldAuditRule {
    override fun check(item: ManualHoldAudit): MutableList<String> {
        if (item.category.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ManualHoldAuditRequiredSummaryRule(var message: String) : ManualHoldAuditRule {
    override fun check(item: ManualHoldAudit): MutableList<String> {
        if (item.summary.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ManualHoldAuditSummaryLengthRule(var maxLength: Int) : ManualHoldAuditRule {
    override fun check(item: ManualHoldAudit): MutableList<String> {
        val size = item.summary.length
        if (size > maxLength) {
            return mutableListOf<String>("summary is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ManualHoldAuditAllowedSummaryRule(var allowed: MutableList<String>) : ManualHoldAuditRule {
    override fun check(item: ManualHoldAudit): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.summary))) {
            return mutableListOf<String>("summary ${item.summary} is above maximum")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a manual hold audit. */
data class ManualHoldAuditValidator(var rules: MutableList<ManualHoldAuditRule>) {
    fun validate(item: ManualHoldAudit): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ManualHoldAudit) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ManualHoldAudit>): MutableMap<String, MutableList<String>> {
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

fun defaultManualHoldAuditValidator(): ManualHoldAuditValidator {
    return ManualHoldAuditValidator(
        mutableListOf<ManualHoldAuditRule>(
            ManualHoldAuditNameLengthRule(255),
            ManualHoldAuditRequiredCategoryRule("manual hold audit needs this field"),
            ManualHoldAuditRequiredSummaryRule("manual hold audit needs this field"),
            ManualHoldAuditSummaryLengthRule(32),
            ManualHoldAuditAllowedSummaryRule(mutableListOf<String>("batched", "rolling")),
        ),
    )
}
