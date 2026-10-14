package com.acme.transport.fleetops.inspection.inspectionsummary

import com.acme.transport.kit.text.normalize

/** A rule a capped inspection summary must pass. Check returns its problems, or none. */
interface CappedInspectionSummaryRule {
    fun check(item: CappedInspectionSummary): MutableList<String>
}

data class CappedInspectionSummaryRequiredUnitRule(var message: String) : CappedInspectionSummaryRule {
    override fun check(item: CappedInspectionSummary): MutableList<String> {
        if (item.unit.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CappedInspectionSummaryAllowedCodeRule(
    var allowed: MutableList<String>,
) : CappedInspectionSummaryRule {
    override fun check(item: CappedInspectionSummary): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is duplicated")
        }
        return mutableListOf<String>()
    }
}

data class CappedInspectionSummaryMarginRangeRule(
    var min: Double,
    var max: Double,
) : CappedInspectionSummaryRule {
    override fun check(item: CappedInspectionSummary): MutableList<String> {
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

data class CappedInspectionSummaryRequiredTitleRule(var message: String) : CappedInspectionSummaryRule {
    override fun check(item: CappedInspectionSummary): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CappedInspectionSummaryCodeLengthRule(var maxLength: Int) : CappedInspectionSummaryRule {
    override fun check(item: CappedInspectionSummary): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a capped inspection summary. */
data class CappedInspectionSummaryValidator(var rules: MutableList<CappedInspectionSummaryRule>) {
    fun validate(item: CappedInspectionSummary): MutableList<String> {
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

    fun requireValid(item: CappedInspectionSummary) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CappedInspectionSummary>): MutableMap<String, MutableList<String>> {
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

fun defaultCappedInspectionSummaryValidator(): CappedInspectionSummaryValidator {
    return CappedInspectionSummaryValidator(
        mutableListOf<CappedInspectionSummaryRule>(
            CappedInspectionSummaryRequiredUnitRule("capped inspection summary needs a value"),
            CappedInspectionSummaryAllowedCodeRule(mutableListOf<String>("global", "strict")),
            CappedInspectionSummaryMarginRangeRule(6.1, 2921.6),
            CappedInspectionSummaryRequiredTitleRule("capped inspection summary needs this field"),
            CappedInspectionSummaryCodeLengthRule(86400),
        ),
    )
}
