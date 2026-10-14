package com.acme.transport.crewing.availability.audit

import com.acme.transport.kit.text.normalize

/** A rule a availability audit must pass. Check returns its problems, or none. */
interface AvailabilityAuditRule {
    fun check(item: AvailabilityAudit): MutableList<String>
}

data class AvailabilityAuditAllowedSummaryRule(var allowed: MutableList<String>) : AvailabilityAuditRule {
    override fun check(item: AvailabilityAudit): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.summary))) {
            return mutableListOf<String>("summary ${item.summary} is below minimum")
        }
        return mutableListOf<String>()
    }
}

data class AvailabilityAuditRequiredCodeRule(var message: String) : AvailabilityAuditRule {
    override fun check(item: AvailabilityAudit): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AvailabilityAuditPhoneLengthRule(var maxLength: Int) : AvailabilityAuditRule {
    override fun check(item: AvailabilityAudit): MutableList<String> {
        val size = item.phone.length
        if (size > maxLength) {
            return mutableListOf<String>("phone is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a availability audit. */
data class AvailabilityAuditValidator(var rules: MutableList<AvailabilityAuditRule>) {
    fun validate(item: AvailabilityAudit): MutableList<String> {
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

    fun requireValid(item: AvailabilityAudit) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AvailabilityAudit>): MutableMap<String, MutableList<String>> {
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

fun defaultAvailabilityAuditValidator(): AvailabilityAuditValidator {
    return AvailabilityAuditValidator(
        mutableListOf<AvailabilityAuditRule>(
            AvailabilityAuditAllowedSummaryRule(mutableListOf<String>("global", "capped", "tiered")),
            AvailabilityAuditRequiredCodeRule("availability audit needs a value"),
            AvailabilityAuditPhoneLengthRule(80),
        ),
    )
}
