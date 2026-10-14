package com.acme.transport.emissions.carbonreport.policy

/** A rule a carbon report summary must pass. Check returns its problems, or none. */
interface CarbonReportSummaryRule {
    fun check(item: CarbonReportSummary): MutableList<String>
}

data class CarbonReportSummaryRequiredCodeRule(var message: String) : CarbonReportSummaryRule {
    override fun check(item: CarbonReportSummary): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CarbonReportSummaryCodePrefixRule(var prefix: String) : CarbonReportSummaryRule {
    override fun check(item: CarbonReportSummary): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class CarbonReportSummaryPalletsRangeRule(var min: Int, var max: Int) : CarbonReportSummaryRule {
    override fun check(item: CarbonReportSummary): MutableList<String> {
        val value = item.pallets
        if (value < min || value > max) {
            return mutableListOf<String>("pallets $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a carbon report summary. */
data class CarbonReportSummaryValidator(var rules: MutableList<CarbonReportSummaryRule>) {
    fun validate(item: CarbonReportSummary): MutableList<String> {
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

    fun requireValid(item: CarbonReportSummary) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CarbonReportSummary>): MutableMap<String, MutableList<String>> {
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

fun defaultCarbonReportSummaryValidator(): CarbonReportSummaryValidator {
    return CarbonReportSummaryValidator(
        mutableListOf<CarbonReportSummaryRule>(
            CarbonReportSummaryRequiredCodeRule("carbon report summary needs a value"),
            CarbonReportSummaryCodePrefixRule("Q"),
            CarbonReportSummaryPalletsRangeRule(1000, 3600),
        ),
    )
}
