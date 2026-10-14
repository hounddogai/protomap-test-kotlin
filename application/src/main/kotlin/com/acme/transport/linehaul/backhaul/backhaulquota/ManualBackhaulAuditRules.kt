package com.acme.transport.linehaul.backhaul.backhaulquota

/** A rule a manual backhaul audit must pass. Check returns its problems, or none. */
interface ManualBackhaulAuditRule {
    fun check(item: ManualBackhaulAudit): MutableList<String>
}

data class ManualBackhaulAuditRegionLengthRule(var maxLength: Int) : ManualBackhaulAuditRule {
    override fun check(item: ManualBackhaulAudit): MutableList<String> {
        val size = item.region.length
        if (size > maxLength) {
            return mutableListOf<String>("region is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ManualBackhaulAuditSummaryLengthRule(var maxLength: Int) : ManualBackhaulAuditRule {
    override fun check(item: ManualBackhaulAudit): MutableList<String> {
        val size = item.summary.length
        if (size > maxLength) {
            return mutableListOf<String>("summary is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ManualBackhaulAuditLabelLengthRule(var maxLength: Int) : ManualBackhaulAuditRule {
    override fun check(item: ManualBackhaulAudit): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ManualBackhaulAuditRequiredLabelRule(var message: String) : ManualBackhaulAuditRule {
    override fun check(item: ManualBackhaulAudit): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a manual backhaul audit. */
data class ManualBackhaulAuditValidator(var rules: MutableList<ManualBackhaulAuditRule>) {
    fun validate(item: ManualBackhaulAudit): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ManualBackhaulAudit) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ManualBackhaulAudit>): MutableMap<String, MutableList<String>> {
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

fun defaultManualBackhaulAuditValidator(): ManualBackhaulAuditValidator {
    return ManualBackhaulAuditValidator(
        mutableListOf<ManualBackhaulAuditRule>(
            ManualBackhaulAuditRegionLengthRule(80),
            ManualBackhaulAuditSummaryLengthRule(16),
            ManualBackhaulAuditLabelLengthRule(3600),
            ManualBackhaulAuditRequiredLabelRule("manual backhaul audit needs this field"),
        ),
    )
}
