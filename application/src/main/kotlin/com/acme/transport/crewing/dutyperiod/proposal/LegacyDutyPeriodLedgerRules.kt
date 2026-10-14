package com.acme.transport.crewing.dutyperiod.proposal

import com.acme.transport.kit.text.normalize

/** A rule a legacy duty period ledger must pass. Check returns its problems, or none. */
interface LegacyDutyPeriodLedgerRule {
    fun check(item: LegacyDutyPeriodLedger): MutableList<String>
}

data class LegacyDutyPeriodLedgerExternalRefLengthRule(var maxLength: Int) : LegacyDutyPeriodLedgerRule {
    override fun check(item: LegacyDutyPeriodLedger): MutableList<String> {
        val size = item.externalRef.length
        if (size > maxLength) {
            return mutableListOf<String>("external ref is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LegacyDutyPeriodLedgerAllowedCodeRule(
    var allowed: MutableList<String>,
) : LegacyDutyPeriodLedgerRule {
    override fun check(item: LegacyDutyPeriodLedger): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is too long")
        }
        return mutableListOf<String>()
    }
}

data class LegacyDutyPeriodLedgerQuantityRangeRule(var min: Int, var max: Int) : LegacyDutyPeriodLedgerRule {
    override fun check(item: LegacyDutyPeriodLedger): MutableList<String> {
        val value = item.quantity
        if (value < min || value > max) {
            return mutableListOf<String>("quantity $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class LegacyDutyPeriodLedgerCodePrefixRule(var prefix: String) : LegacyDutyPeriodLedgerRule {
    override fun check(item: LegacyDutyPeriodLedger): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a legacy duty period ledger. */
data class LegacyDutyPeriodLedgerValidator(var rules: MutableList<LegacyDutyPeriodLedgerRule>) {
    fun validate(item: LegacyDutyPeriodLedger): MutableList<String> {
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

    fun requireValid(item: LegacyDutyPeriodLedger) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LegacyDutyPeriodLedger>): MutableMap<String, MutableList<String>> {
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

fun defaultLegacyDutyPeriodLedgerValidator(): LegacyDutyPeriodLedgerValidator {
    return LegacyDutyPeriodLedgerValidator(
        mutableListOf<LegacyDutyPeriodLedgerRule>(
            LegacyDutyPeriodLedgerExternalRefLengthRule(255),
            LegacyDutyPeriodLedgerAllowedCodeRule(mutableListOf<String>("primary", "legacy", "manual")),
            LegacyDutyPeriodLedgerQuantityRangeRule(32, 3600),
            LegacyDutyPeriodLedgerCodePrefixRule("OPS"),
        ),
    )
}
