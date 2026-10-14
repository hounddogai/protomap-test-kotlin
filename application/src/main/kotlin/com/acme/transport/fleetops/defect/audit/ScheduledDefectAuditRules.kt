package com.acme.transport.fleetops.defect.audit

/** A rule a scheduled defect audit must pass. Check returns its problems, or none. */
interface ScheduledDefectAuditRule {
    fun check(item: ScheduledDefectAudit): MutableList<String>
}

data class ScheduledDefectAuditRequiredLabelRule(var message: String) : ScheduledDefectAuditRule {
    override fun check(item: ScheduledDefectAudit): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ScheduledDefectAuditSegmentLengthRule(var maxLength: Int) : ScheduledDefectAuditRule {
    override fun check(item: ScheduledDefectAudit): MutableList<String> {
        val size = item.segment.length
        if (size > maxLength) {
            return mutableListOf<String>("segment is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledDefectAuditTagsLimitRule(
    var maxCount: Int,
    var banned: MutableList<String>,
) : ScheduledDefectAuditRule {
    override fun check(item: ScheduledDefectAudit): MutableList<String> {
        val problems = mutableListOf<String>()
        if (item.tags.size > maxCount) {
            problems.add("has ${item.tags.size} tags, at most $maxCount allowed")
        }
        for (tag in item.tags) {
            if (banned.contains(tag)) {
                problems.add("tag $tag is not allowed")
            }
        }
        return problems
    }
}

data class ScheduledDefectAuditCodePrefixRule(var prefix: String) : ScheduledDefectAuditRule {
    override fun check(item: ScheduledDefectAudit): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledDefectAuditRequiredCodeRule(var message: String) : ScheduledDefectAuditRule {
    override fun check(item: ScheduledDefectAudit): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a scheduled defect audit. */
data class ScheduledDefectAuditValidator(var rules: MutableList<ScheduledDefectAuditRule>) {
    fun validate(item: ScheduledDefectAudit): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ScheduledDefectAudit) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ScheduledDefectAudit>): MutableMap<String, MutableList<String>> {
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

fun defaultScheduledDefectAuditValidator(): ScheduledDefectAuditValidator {
    return ScheduledDefectAuditValidator(
        mutableListOf<ScheduledDefectAuditRule>(
            ScheduledDefectAuditRequiredLabelRule("scheduled defect audit needs an entry"),
            ScheduledDefectAuditSegmentLengthRule(32),
            ScheduledDefectAuditTagsLimitRule(16, mutableListOf<String>()),
            ScheduledDefectAuditCodePrefixRule("X"),
            ScheduledDefectAuditRequiredCodeRule("scheduled defect audit needs an entry"),
        ),
    )
}
