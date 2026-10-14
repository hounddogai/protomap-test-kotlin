package com.acme.transport.linehaul.backhaul.backhaulaudit

/** A rule a lenient backhaul audit must pass. Check returns its problems, or none. */
interface LenientBackhaulAuditRule {
    fun check(item: LenientBackhaulAudit): MutableList<String>
}

data class LenientBackhaulAuditAmountRangeRule(var min: Double, var max: Double) : LenientBackhaulAuditRule {
    override fun check(item: LenientBackhaulAudit): MutableList<String> {
        val value = item.amount
        if (value < min || value > max) {
            return mutableListOf<String>("amount $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class LenientBackhaulAuditCodeLengthRule(var maxLength: Int) : LenientBackhaulAuditRule {
    override fun check(item: LenientBackhaulAudit): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LenientBackhaulAuditVisibleGuardRule(var limit: Double) : LenientBackhaulAuditRule {
    override fun check(item: LenientBackhaulAudit): MutableList<String> {
        if (item.visible && item.amount > limit) {
            return mutableListOf<String>("amount ${item.amount} exceeds $limit while visible")
        }
        return mutableListOf<String>()
    }
}

data class LenientBackhaulAuditShiftCodeLengthRule(var maxLength: Int) : LenientBackhaulAuditRule {
    override fun check(item: LenientBackhaulAudit): MutableList<String> {
        val size = item.shiftCode.length
        if (size > maxLength) {
            return mutableListOf<String>("shift code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LenientBackhaulAuditRequiredNameRule(var message: String) : LenientBackhaulAuditRule {
    override fun check(item: LenientBackhaulAudit): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LenientBackhaulAuditRequiredDriverIdRule(var message: String) : LenientBackhaulAuditRule {
    override fun check(item: LenientBackhaulAudit): MutableList<String> {
        if (item.driverId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a lenient backhaul audit. */
data class LenientBackhaulAuditValidator(var rules: MutableList<LenientBackhaulAuditRule>) {
    fun validate(item: LenientBackhaulAudit): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: LenientBackhaulAudit) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LenientBackhaulAudit>): MutableMap<String, MutableList<String>> {
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

fun defaultLenientBackhaulAuditValidator(): LenientBackhaulAuditValidator {
    return LenientBackhaulAuditValidator(
        mutableListOf<LenientBackhaulAuditRule>(
            LenientBackhaulAuditAmountRangeRule(6.9, 4969.8),
            LenientBackhaulAuditCodeLengthRule(255),
            LenientBackhaulAuditVisibleGuardRule(0.5),
            LenientBackhaulAuditShiftCodeLengthRule(32),
            LenientBackhaulAuditRequiredNameRule("lenient backhaul audit needs an entry"),
            LenientBackhaulAuditRequiredDriverIdRule("lenient backhaul audit needs this field"),
        ),
    )
}
