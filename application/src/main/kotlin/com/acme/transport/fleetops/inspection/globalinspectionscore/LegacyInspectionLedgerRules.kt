package com.acme.transport.fleetops.inspection.globalinspectionscore

import com.acme.transport.kit.text.normalize

/** A rule a legacy inspection ledger must pass. Check returns its problems, or none. */
interface LegacyInspectionLedgerRule {
    fun check(item: LegacyInspectionLedger): MutableList<String>
}

data class LegacyInspectionLedgerAllowedTitleRule(
    var allowed: MutableList<String>,
) : LegacyInspectionLedgerRule {
    override fun check(item: LegacyInspectionLedger): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.title))) {
            return mutableListOf<String>("title ${item.title} is out of range")
        }
        return mutableListOf<String>()
    }
}

data class LegacyInspectionLedgerCodePrefixRule(var prefix: String) : LegacyInspectionLedgerRule {
    override fun check(item: LegacyInspectionLedger): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class LegacyInspectionLedgerRequiredTitleRule(var message: String) : LegacyInspectionLedgerRule {
    override fun check(item: LegacyInspectionLedger): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LegacyInspectionLedgerCodeLengthRule(var maxLength: Int) : LegacyInspectionLedgerRule {
    override fun check(item: LegacyInspectionLedger): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LegacyInspectionLedgerTitleLengthRule(var maxLength: Int) : LegacyInspectionLedgerRule {
    override fun check(item: LegacyInspectionLedger): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LegacyInspectionLedgerRequiredCodeRule(var message: String) : LegacyInspectionLedgerRule {
    override fun check(item: LegacyInspectionLedger): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a legacy inspection ledger. */
data class LegacyInspectionLedgerValidator(var rules: MutableList<LegacyInspectionLedgerRule>) {
    fun validate(item: LegacyInspectionLedger): MutableList<String> {
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

    fun requireValid(item: LegacyInspectionLedger) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LegacyInspectionLedger>): MutableMap<String, MutableList<String>> {
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

fun defaultLegacyInspectionLedgerValidator(): LegacyInspectionLedgerValidator {
    return LegacyInspectionLedgerValidator(
        mutableListOf<LegacyInspectionLedgerRule>(
            LegacyInspectionLedgerAllowedTitleRule(mutableListOf<String>("rolling", "default", "strict")),
            LegacyInspectionLedgerCodePrefixRule("REF"),
            LegacyInspectionLedgerRequiredTitleRule("legacy inspection ledger needs an entry"),
            LegacyInspectionLedgerCodeLengthRule(16),
            LegacyInspectionLedgerTitleLengthRule(3600),
            LegacyInspectionLedgerRequiredCodeRule("legacy inspection ledger needs this field"),
        ),
    )
}
