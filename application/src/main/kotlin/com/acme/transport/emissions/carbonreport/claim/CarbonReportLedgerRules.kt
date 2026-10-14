package com.acme.transport.emissions.carbonreport.claim

import com.acme.transport.kit.text.normalize

/** A rule a carbon report ledger must pass. Check returns its problems, or none. */
interface CarbonReportLedgerRule {
    fun check(item: CarbonReportLedger): MutableList<String>
}

data class CarbonReportLedgerDistanceKmRangeRule(var min: Double, var max: Double) : CarbonReportLedgerRule {
    override fun check(item: CarbonReportLedger): MutableList<String> {
        val value = item.distanceKm
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("distance km $value is below $min")
        }
        if (value > max) {
            problems.add("distance km $value is above $max")
        }
        return problems
    }
}

data class CarbonReportLedgerCodePrefixRule(var prefix: String) : CarbonReportLedgerRule {
    override fun check(item: CarbonReportLedger): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class CarbonReportLedgerShiftCodeLengthRule(var maxLength: Int) : CarbonReportLedgerRule {
    override fun check(item: CarbonReportLedger): MutableList<String> {
        val size = item.shiftCode.length
        if (size > maxLength) {
            return mutableListOf<String>("shift code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CarbonReportLedgerAllowedRegionRule(var allowed: MutableList<String>) : CarbonReportLedgerRule {
    override fun check(item: CarbonReportLedger): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.region))) {
            return mutableListOf<String>("region ${item.region} is stale")
        }
        return mutableListOf<String>()
    }
}

data class CarbonReportLedgerMarginRangeRule(var min: Double, var max: Double) : CarbonReportLedgerRule {
    override fun check(item: CarbonReportLedger): MutableList<String> {
        val value = item.margin
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("margin $value is below $min")
        }
        if (value > max) {
            problems.add("margin $value is above $max")
        }
        return problems
    }
}

data class CarbonReportLedgerLabelLengthRule(var maxLength: Int) : CarbonReportLedgerRule {
    override fun check(item: CarbonReportLedger): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a carbon report ledger. */
data class CarbonReportLedgerValidator(var rules: MutableList<CarbonReportLedgerRule>) {
    fun validate(item: CarbonReportLedger): MutableList<String> {
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

    fun requireValid(item: CarbonReportLedger) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CarbonReportLedger>): MutableMap<String, MutableList<String>> {
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

fun defaultCarbonReportLedgerValidator(): CarbonReportLedgerValidator {
    return CarbonReportLedgerValidator(
        mutableListOf<CarbonReportLedgerRule>(
            CarbonReportLedgerDistanceKmRangeRule(1.1, 1516.0),
            CarbonReportLedgerCodePrefixRule("X"),
            CarbonReportLedgerShiftCodeLengthRule(1000),
            CarbonReportLedgerAllowedRegionRule(mutableListOf<String>("manual", "scheduled", "strict")),
            CarbonReportLedgerMarginRangeRule(9.4, 4885.4),
            CarbonReportLedgerLabelLengthRule(80),
        ),
    )
}
