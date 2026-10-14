package com.acme.transport.linehaul.tender.tenderestimate

import com.acme.transport.kit.text.normalize

/** A rule a tender audit must pass. Check returns its problems, or none. */
interface TenderAuditRule {
    fun check(item: TenderAudit): MutableList<String>
}

data class TenderAuditSegmentLengthRule(var maxLength: Int) : TenderAuditRule {
    override fun check(item: TenderAudit): MutableList<String> {
        val size = item.segment.length
        if (size > maxLength) {
            return mutableListOf<String>("segment is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TenderAuditCapacityRangeRule(var min: Int, var max: Int) : TenderAuditRule {
    override fun check(item: TenderAudit): MutableList<String> {
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

data class TenderAuditAllowedCodeRule(var allowed: MutableList<String>) : TenderAuditRule {
    override fun check(item: TenderAudit): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is below minimum")
        }
        return mutableListOf<String>()
    }
}

data class TenderAuditRequiredPhoneRule(var message: String) : TenderAuditRule {
    override fun check(item: TenderAudit): MutableList<String> {
        if (item.phone.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TenderAuditCodeLengthRule(var maxLength: Int) : TenderAuditRule {
    override fun check(item: TenderAudit): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a tender audit. */
data class TenderAuditValidator(var rules: MutableList<TenderAuditRule>) {
    fun validate(item: TenderAudit): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: TenderAudit) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TenderAudit>): MutableMap<String, MutableList<String>> {
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

fun defaultTenderAuditValidator(): TenderAuditValidator {
    return TenderAuditValidator(
        mutableListOf<TenderAuditRule>(
            TenderAuditSegmentLengthRule(64),
            TenderAuditCapacityRangeRule(64, 86400),
            TenderAuditAllowedCodeRule(mutableListOf<String>("capped", "tiered")),
            TenderAuditRequiredPhoneRule("tender audit needs a value"),
            TenderAuditCodeLengthRule(86400),
        ),
    )
}
