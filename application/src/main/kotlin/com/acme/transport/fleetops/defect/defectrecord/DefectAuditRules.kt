package com.acme.transport.fleetops.defect.defectrecord

import com.acme.transport.kit.text.normalize

/** A rule a defect audit must pass. Check returns its problems, or none. */
interface DefectAuditRule {
    fun check(item: DefectAudit): MutableList<String>
}

data class DefectAuditRequiredNameRule(var message: String) : DefectAuditRule {
    override fun check(item: DefectAudit): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DefectAuditCodeLengthRule(var maxLength: Int) : DefectAuditRule {
    override fun check(item: DefectAudit): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DefectAuditPhoneLengthRule(var maxLength: Int) : DefectAuditRule {
    override fun check(item: DefectAudit): MutableList<String> {
        val size = item.phone.length
        if (size > maxLength) {
            return mutableListOf<String>("phone is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DefectAuditRequiredTimezoneRule(var message: String) : DefectAuditRule {
    override fun check(item: DefectAudit): MutableList<String> {
        if (item.timezone.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DefectAuditAllowedCodeRule(var allowed: MutableList<String>) : DefectAuditRule {
    override fun check(item: DefectAudit): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is above maximum")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a defect audit. */
data class DefectAuditValidator(var rules: MutableList<DefectAuditRule>) {
    fun validate(item: DefectAudit): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: DefectAudit) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DefectAudit>): MutableMap<String, MutableList<String>> {
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

fun defaultDefectAuditValidator(): DefectAuditValidator {
    return DefectAuditValidator(
        mutableListOf<DefectAuditRule>(
            DefectAuditRequiredNameRule("defect audit needs a value"),
            DefectAuditCodeLengthRule(120),
            DefectAuditPhoneLengthRule(3600),
            DefectAuditRequiredTimezoneRule("defect audit needs this field"),
            DefectAuditAllowedCodeRule(mutableListOf<String>("capped", "manual")),
        ),
    )
}
