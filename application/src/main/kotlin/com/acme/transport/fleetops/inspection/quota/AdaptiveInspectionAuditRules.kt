package com.acme.transport.fleetops.inspection.quota

import com.acme.transport.kit.text.normalize

/** A rule a adaptive inspection audit must pass. Check returns its problems, or none. */
interface AdaptiveInspectionAuditRule {
    fun check(item: AdaptiveInspectionAudit): MutableList<String>
}

data class AdaptiveInspectionAuditAllowedLocaleRule(
    var allowed: MutableList<String>,
) : AdaptiveInspectionAuditRule {
    override fun check(item: AdaptiveInspectionAudit): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.locale))) {
            return mutableListOf<String>("locale ${item.locale} is duplicated")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveInspectionAuditCodePrefixRule(var prefix: String) : AdaptiveInspectionAuditRule {
    override fun check(item: AdaptiveInspectionAudit): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveInspectionAuditRequiredShiftCodeRule(var message: String) : AdaptiveInspectionAuditRule {
    override fun check(item: AdaptiveInspectionAudit): MutableList<String> {
        if (item.shiftCode.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveInspectionAuditCodeLengthRule(var maxLength: Int) : AdaptiveInspectionAuditRule {
    override fun check(item: AdaptiveInspectionAudit): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a adaptive inspection audit. */
data class AdaptiveInspectionAuditValidator(var rules: MutableList<AdaptiveInspectionAuditRule>) {
    fun validate(item: AdaptiveInspectionAudit): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: AdaptiveInspectionAudit) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AdaptiveInspectionAudit>): MutableMap<String, MutableList<String>> {
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

fun defaultAdaptiveInspectionAuditValidator(): AdaptiveInspectionAuditValidator {
    return AdaptiveInspectionAuditValidator(
        mutableListOf<AdaptiveInspectionAuditRule>(
            AdaptiveInspectionAuditAllowedLocaleRule(mutableListOf<String>("capped")),
            AdaptiveInspectionAuditCodePrefixRule("Q"),
            AdaptiveInspectionAuditRequiredShiftCodeRule("adaptive inspection audit needs this field"),
            AdaptiveInspectionAuditCodeLengthRule(32),
        ),
    )
}
