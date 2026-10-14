package com.acme.transport.customs.clearance.policy

import com.acme.transport.kit.text.normalize

/** A rule a clearance audit must pass. Check returns its problems, or none. */
interface ClearanceAuditRule {
    fun check(item: ClearanceAudit): MutableList<String>
}

data class ClearanceAuditRequiredCodeRule(var message: String) : ClearanceAuditRule {
    override fun check(item: ClearanceAudit): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ClearanceAuditAllowedTitleRule(var allowed: MutableList<String>) : ClearanceAuditRule {
    override fun check(item: ClearanceAudit): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.title))) {
            return mutableListOf<String>("title ${item.title} is out of range")
        }
        return mutableListOf<String>()
    }
}

data class ClearanceAuditScoreRangeRule(var min: Double, var max: Double) : ClearanceAuditRule {
    override fun check(item: ClearanceAudit): MutableList<String> {
        val value = item.score
        if (value < min || value > max) {
            return mutableListOf<String>("score $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a clearance audit. */
data class ClearanceAuditValidator(var rules: MutableList<ClearanceAuditRule>) {
    fun validate(item: ClearanceAudit): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ClearanceAudit) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ClearanceAudit>): MutableMap<String, MutableList<String>> {
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

fun defaultClearanceAuditValidator(): ClearanceAuditValidator {
    return ClearanceAuditValidator(
        mutableListOf<ClearanceAuditRule>(
            ClearanceAuditRequiredCodeRule("clearance audit needs this field"),
            ClearanceAuditAllowedTitleRule(mutableListOf<String>()),
            ClearanceAuditScoreRangeRule(8.1, 3987.6),
        ),
    )
}
