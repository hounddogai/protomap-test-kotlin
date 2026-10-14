package com.acme.transport.freightpay.accessorial.audit

/** A rule a capped accessorial audit must pass. Check returns its problems, or none. */
interface CappedAccessorialAuditRule {
    fun check(item: CappedAccessorialAudit): MutableList<String>
}

data class CappedAccessorialAuditPhoneLengthRule(var maxLength: Int) : CappedAccessorialAuditRule {
    override fun check(item: CappedAccessorialAudit): MutableList<String> {
        val size = item.phone.length
        if (size > maxLength) {
            return mutableListOf<String>("phone is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CappedAccessorialAuditDriverIdLengthRule(var maxLength: Int) : CappedAccessorialAuditRule {
    override fun check(item: CappedAccessorialAudit): MutableList<String> {
        val size = item.driverId.length
        if (size > maxLength) {
            return mutableListOf<String>("driver id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CappedAccessorialAuditBudgetRangeRule(
    var min: Double,
    var max: Double,
) : CappedAccessorialAuditRule {
    override fun check(item: CappedAccessorialAudit): MutableList<String> {
        val value = item.budget
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("budget $value is below $min")
        }
        if (value > max) {
            problems.add("budget $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a capped accessorial audit. */
data class CappedAccessorialAuditValidator(var rules: MutableList<CappedAccessorialAuditRule>) {
    fun validate(item: CappedAccessorialAudit): MutableList<String> {
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

    fun requireValid(item: CappedAccessorialAudit) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CappedAccessorialAudit>): MutableMap<String, MutableList<String>> {
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

fun defaultCappedAccessorialAuditValidator(): CappedAccessorialAuditValidator {
    return CappedAccessorialAuditValidator(
        mutableListOf<CappedAccessorialAuditRule>(
            CappedAccessorialAuditPhoneLengthRule(3600),
            CappedAccessorialAuditDriverIdLengthRule(86400),
            CappedAccessorialAuditBudgetRangeRule(3.6, 1037.3),
        ),
    )
}
