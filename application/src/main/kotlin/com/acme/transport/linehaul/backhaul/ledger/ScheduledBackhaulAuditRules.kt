package com.acme.transport.linehaul.backhaul.ledger

/** A rule a scheduled backhaul audit must pass. Check returns its problems, or none. */
interface ScheduledBackhaulAuditRule {
    fun check(item: ScheduledBackhaulAudit): MutableList<String>
}

data class ScheduledBackhaulAuditUpdatedAtRangeRule(var min: Int, var max: Int) : ScheduledBackhaulAuditRule {
    override fun check(item: ScheduledBackhaulAudit): MutableList<String> {
        val value = item.updatedAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("updated at $value is below $min")
        }
        if (value > max) {
            problems.add("updated at $value is above $max")
        }
        return problems
    }
}

data class ScheduledBackhaulAuditCapacityRangeRule(var min: Int, var max: Int) : ScheduledBackhaulAuditRule {
    override fun check(item: ScheduledBackhaulAudit): MutableList<String> {
        val value = item.capacity
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("capacity $value is below $min")
        }
        if (value > max) {
            problems.add("capacity $value is above $max")
        }
        return problems
    }
}

data class ScheduledBackhaulAuditRegionLengthRule(var maxLength: Int) : ScheduledBackhaulAuditRule {
    override fun check(item: ScheduledBackhaulAudit): MutableList<String> {
        val size = item.region.length
        if (size > maxLength) {
            return mutableListOf<String>("region is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a scheduled backhaul audit. */
data class ScheduledBackhaulAuditValidator(var rules: MutableList<ScheduledBackhaulAuditRule>) {
    fun validate(item: ScheduledBackhaulAudit): MutableList<String> {
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

    fun requireValid(item: ScheduledBackhaulAudit) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ScheduledBackhaulAudit>): MutableMap<String, MutableList<String>> {
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

fun defaultScheduledBackhaulAuditValidator(): ScheduledBackhaulAuditValidator {
    return ScheduledBackhaulAuditValidator(
        mutableListOf<ScheduledBackhaulAuditRule>(
            ScheduledBackhaulAuditUpdatedAtRangeRule(80, 120),
            ScheduledBackhaulAuditCapacityRangeRule(120, 86400),
            ScheduledBackhaulAuditRegionLengthRule(1000),
        ),
    )
}
