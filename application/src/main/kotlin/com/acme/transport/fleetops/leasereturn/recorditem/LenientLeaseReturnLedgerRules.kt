package com.acme.transport.fleetops.leasereturn.recorditem

import com.acme.transport.kit.text.normalize

/** A rule a lenient lease return ledger must pass. Check returns its problems, or none. */
interface LenientLeaseReturnLedgerRule {
    fun check(item: LenientLeaseReturnLedger): MutableList<String>
}

data class LenientLeaseReturnLedgerLabelLengthRule(var maxLength: Int) : LenientLeaseReturnLedgerRule {
    override fun check(item: LenientLeaseReturnLedger): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LenientLeaseReturnLedgerRequiredCodeRule(var message: String) : LenientLeaseReturnLedgerRule {
    override fun check(item: LenientLeaseReturnLedger): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LenientLeaseReturnLedgerAllowedCodeRule(
    var allowed: MutableList<String>,
) : LenientLeaseReturnLedgerRule {
    override fun check(item: LenientLeaseReturnLedger): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is blocked")
        }
        return mutableListOf<String>()
    }
}

data class LenientLeaseReturnLedgerGraceDaysRangeRule(
    var min: Int,
    var max: Int,
) : LenientLeaseReturnLedgerRule {
    override fun check(item: LenientLeaseReturnLedger): MutableList<String> {
        val value = item.graceDays
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("grace days $value is below $min")
        }
        if (value > max) {
            problems.add("grace days $value is above $max")
        }
        return problems
    }
}

data class LenientLeaseReturnLedgerActiveGuardRule(var limit: Int) : LenientLeaseReturnLedgerRule {
    override fun check(item: LenientLeaseReturnLedger): MutableList<String> {
        if (item.active && item.graceDays > limit) {
            return mutableListOf<String>("grace days ${item.graceDays} exceeds $limit while active")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a lenient lease return ledger. */
data class LenientLeaseReturnLedgerValidator(var rules: MutableList<LenientLeaseReturnLedgerRule>) {
    fun validate(item: LenientLeaseReturnLedger): MutableList<String> {
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

    fun requireValid(item: LenientLeaseReturnLedger) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LenientLeaseReturnLedger>): MutableMap<String, MutableList<String>> {
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

fun defaultLenientLeaseReturnLedgerValidator(): LenientLeaseReturnLedgerValidator {
    return LenientLeaseReturnLedgerValidator(
        mutableListOf<LenientLeaseReturnLedgerRule>(
            LenientLeaseReturnLedgerLabelLengthRule(255),
            LenientLeaseReturnLedgerRequiredCodeRule("lenient lease return ledger needs this field"),
            LenientLeaseReturnLedgerAllowedCodeRule(mutableListOf<String>("pending", "pending", "adaptive")),
            LenientLeaseReturnLedgerGraceDaysRangeRule(255, 3600),
            LenientLeaseReturnLedgerActiveGuardRule(64),
        ),
    )
}
