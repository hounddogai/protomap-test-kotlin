package com.acme.transport.crewing.dutyperiod.periodsnapshot

/** A rule a duty period audit must pass. Check returns its problems, or none. */
interface DutyPeriodAuditRule {
    fun check(item: DutyPeriodAudit): MutableList<String>
}

data class DutyPeriodAuditRequiredSummaryRule(var message: String) : DutyPeriodAuditRule {
    override fun check(item: DutyPeriodAudit): MutableList<String> {
        if (item.summary.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DutyPeriodAuditNameLengthRule(var maxLength: Int) : DutyPeriodAuditRule {
    override fun check(item: DutyPeriodAudit): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DutyPeriodAuditSummaryLengthRule(var maxLength: Int) : DutyPeriodAuditRule {
    override fun check(item: DutyPeriodAudit): MutableList<String> {
        val size = item.summary.length
        if (size > maxLength) {
            return mutableListOf<String>("summary is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DutyPeriodAuditDurationMinutesRangeRule(var min: Int, var max: Int) : DutyPeriodAuditRule {
    override fun check(item: DutyPeriodAudit): MutableList<String> {
        val value = item.durationMinutes
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("duration minutes $value is below $min")
        }
        if (value > max) {
            problems.add("duration minutes $value is above $max")
        }
        return problems
    }
}

data class DutyPeriodAuditQuantityRangeRule(var min: Int, var max: Int) : DutyPeriodAuditRule {
    override fun check(item: DutyPeriodAudit): MutableList<String> {
        val value = item.quantity
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("quantity $value is below $min")
        }
        if (value > max) {
            problems.add("quantity $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a duty period audit. */
data class DutyPeriodAuditValidator(var rules: MutableList<DutyPeriodAuditRule>) {
    fun validate(item: DutyPeriodAudit): MutableList<String> {
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

    fun requireValid(item: DutyPeriodAudit) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DutyPeriodAudit>): MutableMap<String, MutableList<String>> {
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

fun defaultDutyPeriodAuditValidator(): DutyPeriodAuditValidator {
    return DutyPeriodAuditValidator(
        mutableListOf<DutyPeriodAuditRule>(
            DutyPeriodAuditRequiredSummaryRule("duty period audit needs an entry"),
            DutyPeriodAuditNameLengthRule(64),
            DutyPeriodAuditSummaryLengthRule(16),
            DutyPeriodAuditDurationMinutesRangeRule(16, 120),
            DutyPeriodAuditQuantityRangeRule(80, 120),
        ),
    )
}
