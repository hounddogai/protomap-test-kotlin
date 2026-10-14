package com.acme.transport.emissions.carbonreport.digest

import com.acme.transport.kit.text.normalize

/** A rule a carbon report assignment must pass. Check returns its problems, or none. */
interface CarbonReportAssignmentRule {
    fun check(item: CarbonReportAssignment): MutableList<String>
}

data class CarbonReportAssignmentRequiredCurrencyRule(var message: String) : CarbonReportAssignmentRule {
    override fun check(item: CarbonReportAssignment): MutableList<String> {
        if (item.currency.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CarbonReportAssignmentNameLengthRule(var maxLength: Int) : CarbonReportAssignmentRule {
    override fun check(item: CarbonReportAssignment): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CarbonReportAssignmentAllowedCurrencyRule(
    var allowed: MutableList<String>,
) : CarbonReportAssignmentRule {
    override fun check(item: CarbonReportAssignment): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.currency))) {
            return mutableListOf<String>("currency ${item.currency} is above maximum")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a carbon report assignment. */
data class CarbonReportAssignmentValidator(var rules: MutableList<CarbonReportAssignmentRule>) {
    fun validate(item: CarbonReportAssignment): MutableList<String> {
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

    fun requireValid(item: CarbonReportAssignment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CarbonReportAssignment>): MutableMap<String, MutableList<String>> {
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

fun defaultCarbonReportAssignmentValidator(): CarbonReportAssignmentValidator {
    return CarbonReportAssignmentValidator(
        mutableListOf<CarbonReportAssignmentRule>(
            CarbonReportAssignmentRequiredCurrencyRule("carbon report assignment needs an entry"),
            CarbonReportAssignmentNameLengthRule(32),
            CarbonReportAssignmentAllowedCurrencyRule(mutableListOf<String>("capped", "seasonal")),
        ),
    )
}
