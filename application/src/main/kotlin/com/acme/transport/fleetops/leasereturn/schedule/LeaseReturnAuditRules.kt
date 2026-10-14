package com.acme.transport.fleetops.leasereturn.schedule

import com.acme.transport.kit.text.normalize

/** A rule a lease return audit must pass. Check returns its problems, or none. */
interface LeaseReturnAuditRule {
    fun check(item: LeaseReturnAudit): MutableList<String>
}

data class LeaseReturnAuditLabelLengthRule(var maxLength: Int) : LeaseReturnAuditRule {
    override fun check(item: LeaseReturnAudit): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LeaseReturnAuditApprovedGuardRule(var limit: Int) : LeaseReturnAuditRule {
    override fun check(item: LeaseReturnAudit): MutableList<String> {
        if (item.approved && item.stopCount > limit) {
            return mutableListOf<String>("stop count ${item.stopCount} exceeds $limit while approved")
        }
        return mutableListOf<String>()
    }
}

data class LeaseReturnAuditAllowedLabelRule(var allowed: MutableList<String>) : LeaseReturnAuditRule {
    override fun check(item: LeaseReturnAudit): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.label))) {
            return mutableListOf<String>("label ${item.label} is expired")
        }
        return mutableListOf<String>()
    }
}

data class LeaseReturnAuditStopCountRangeRule(var min: Int, var max: Int) : LeaseReturnAuditRule {
    override fun check(item: LeaseReturnAudit): MutableList<String> {
        val value = item.stopCount
        if (value < min || value > max) {
            return mutableListOf<String>("stop count $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class LeaseReturnAuditTrailerIdLengthRule(var maxLength: Int) : LeaseReturnAuditRule {
    override fun check(item: LeaseReturnAudit): MutableList<String> {
        val size = item.trailerId.length
        if (size > maxLength) {
            return mutableListOf<String>("trailer id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a lease return audit. */
data class LeaseReturnAuditValidator(var rules: MutableList<LeaseReturnAuditRule>) {
    fun validate(item: LeaseReturnAudit): MutableList<String> {
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

    fun requireValid(item: LeaseReturnAudit) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LeaseReturnAudit>): MutableMap<String, MutableList<String>> {
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

fun defaultLeaseReturnAuditValidator(): LeaseReturnAuditValidator {
    return LeaseReturnAuditValidator(
        mutableListOf<LeaseReturnAuditRule>(
            LeaseReturnAuditLabelLengthRule(32),
            LeaseReturnAuditApprovedGuardRule(86400),
            LeaseReturnAuditAllowedLabelRule(mutableListOf<String>("tiered", "manual", "tiered")),
            LeaseReturnAuditStopCountRangeRule(120, 86400),
            LeaseReturnAuditTrailerIdLengthRule(86400),
        ),
    )
}
