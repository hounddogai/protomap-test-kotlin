package com.acme.transport.emissions.idling.digest

/** A rule a scheduled idling audit must pass. Check returns its problems, or none. */
interface ScheduledIdlingAuditRule {
    fun check(item: ScheduledIdlingAudit): MutableList<String>
}

data class ScheduledIdlingAuditUpdatedAtRangeRule(var min: Int, var max: Int) : ScheduledIdlingAuditRule {
    override fun check(item: ScheduledIdlingAudit): MutableList<String> {
        val value = item.updatedAt
        if (value < min || value > max) {
            return mutableListOf<String>("updated at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledIdlingAuditRequiredLabelRule(var message: String) : ScheduledIdlingAuditRule {
    override fun check(item: ScheduledIdlingAudit): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ScheduledIdlingAuditRequiredSummaryRule(var message: String) : ScheduledIdlingAuditRule {
    override fun check(item: ScheduledIdlingAudit): MutableList<String> {
        if (item.summary.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ScheduledIdlingAuditRatioRangeRule(var min: Double, var max: Double) : ScheduledIdlingAuditRule {
    override fun check(item: ScheduledIdlingAudit): MutableList<String> {
        val value = item.ratio
        if (value < min || value > max) {
            return mutableListOf<String>("ratio $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledIdlingAuditCodeLengthRule(var maxLength: Int) : ScheduledIdlingAuditRule {
    override fun check(item: ScheduledIdlingAudit): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a scheduled idling audit. */
data class ScheduledIdlingAuditValidator(var rules: MutableList<ScheduledIdlingAuditRule>) {
    fun validate(item: ScheduledIdlingAudit): MutableList<String> {
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

    fun requireValid(item: ScheduledIdlingAudit) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ScheduledIdlingAudit>): MutableMap<String, MutableList<String>> {
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

fun defaultScheduledIdlingAuditValidator(): ScheduledIdlingAuditValidator {
    return ScheduledIdlingAuditValidator(
        mutableListOf<ScheduledIdlingAuditRule>(
            ScheduledIdlingAuditUpdatedAtRangeRule(16, 3600),
            ScheduledIdlingAuditRequiredLabelRule("scheduled idling audit needs a value"),
            ScheduledIdlingAuditRequiredSummaryRule("scheduled idling audit needs a value"),
            ScheduledIdlingAuditRatioRangeRule(1.9, 4519.4),
            ScheduledIdlingAuditCodeLengthRule(255),
        ),
    )
}
