package com.acme.transport.terminal.crossdock.audit

import com.acme.transport.kit.text.normalize

/** A rule a global cross dock audit must pass. Check returns its problems, or none. */
interface GlobalCrossDockAuditRule {
    fun check(item: GlobalCrossDockAudit): MutableList<String>
}

data class GlobalCrossDockAuditAllowedNameRule(var allowed: MutableList<String>) : GlobalCrossDockAuditRule {
    override fun check(item: GlobalCrossDockAudit): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.name))) {
            return mutableListOf<String>("name ${item.name} is not allowed")
        }
        return mutableListOf<String>()
    }
}

data class GlobalCrossDockAuditLabelLengthRule(var maxLength: Int) : GlobalCrossDockAuditRule {
    override fun check(item: GlobalCrossDockAudit): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class GlobalCrossDockAuditRequiredContactEmailRule(var message: String) : GlobalCrossDockAuditRule {
    override fun check(item: GlobalCrossDockAudit): MutableList<String> {
        if (item.contactEmail.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a global cross dock audit. */
data class GlobalCrossDockAuditValidator(var rules: MutableList<GlobalCrossDockAuditRule>) {
    fun validate(item: GlobalCrossDockAudit): MutableList<String> {
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

    fun requireValid(item: GlobalCrossDockAudit) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<GlobalCrossDockAudit>): MutableMap<String, MutableList<String>> {
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

fun defaultGlobalCrossDockAuditValidator(): GlobalCrossDockAuditValidator {
    return GlobalCrossDockAuditValidator(
        mutableListOf<GlobalCrossDockAuditRule>(
            GlobalCrossDockAuditAllowedNameRule(mutableListOf<String>("fallback", "default", "adaptive")),
            GlobalCrossDockAuditLabelLengthRule(16),
            GlobalCrossDockAuditRequiredContactEmailRule("global cross dock audit needs a value"),
        ),
    )
}
