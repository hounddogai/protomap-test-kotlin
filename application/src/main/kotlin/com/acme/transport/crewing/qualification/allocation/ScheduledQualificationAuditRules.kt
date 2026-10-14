package com.acme.transport.crewing.qualification.allocation

/** A rule a scheduled qualification audit must pass. Check returns its problems, or none. */
interface ScheduledQualificationAuditRule {
    fun check(item: ScheduledQualificationAudit): MutableList<String>
}

data class ScheduledQualificationAuditTitleLengthRule(var maxLength: Int) : ScheduledQualificationAuditRule {
    override fun check(item: ScheduledQualificationAudit): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledQualificationAuditCodePrefixRule(var prefix: String) : ScheduledQualificationAuditRule {
    override fun check(item: ScheduledQualificationAudit): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledQualificationAuditCostRangeRule(
    var min: Double,
    var max: Double,
) : ScheduledQualificationAuditRule {
    override fun check(item: ScheduledQualificationAudit): MutableList<String> {
        val value = item.cost
        if (value < min || value > max) {
            return mutableListOf<String>("cost $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a scheduled qualification audit. */
data class ScheduledQualificationAuditValidator(var rules: MutableList<ScheduledQualificationAuditRule>) {
    fun validate(item: ScheduledQualificationAudit): MutableList<String> {
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

    fun requireValid(item: ScheduledQualificationAudit) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ScheduledQualificationAudit>): MutableMap<String, MutableList<String>> {
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

fun defaultScheduledQualificationAuditValidator(): ScheduledQualificationAuditValidator {
    return ScheduledQualificationAuditValidator(
        mutableListOf<ScheduledQualificationAuditRule>(
            ScheduledQualificationAuditTitleLengthRule(32),
            ScheduledQualificationAuditCodePrefixRule("REF"),
            ScheduledQualificationAuditCostRangeRule(9.0, 2007.2),
        ),
    )
}
