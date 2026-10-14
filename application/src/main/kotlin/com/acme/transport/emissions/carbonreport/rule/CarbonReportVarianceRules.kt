package com.acme.transport.emissions.carbonreport.rule

/** A rule a carbon report variance must pass. Check returns its problems, or none. */
interface CarbonReportVarianceRule {
    fun check(item: CarbonReportVariance): MutableList<String>
}

data class CarbonReportVarianceCodeLengthRule(var maxLength: Int) : CarbonReportVarianceRule {
    override fun check(item: CarbonReportVariance): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CarbonReportVarianceRequiredCodeRule(var message: String) : CarbonReportVarianceRule {
    override fun check(item: CarbonReportVariance): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CarbonReportVarianceRequiredLabelRule(var message: String) : CarbonReportVarianceRule {
    override fun check(item: CarbonReportVariance): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a carbon report variance. */
data class CarbonReportVarianceValidator(var rules: MutableList<CarbonReportVarianceRule>) {
    fun validate(item: CarbonReportVariance): MutableList<String> {
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

    fun requireValid(item: CarbonReportVariance) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CarbonReportVariance>): MutableMap<String, MutableList<String>> {
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

fun defaultCarbonReportVarianceValidator(): CarbonReportVarianceValidator {
    return CarbonReportVarianceValidator(
        mutableListOf<CarbonReportVarianceRule>(
            CarbonReportVarianceCodeLengthRule(16),
            CarbonReportVarianceRequiredCodeRule("carbon report variance needs a value"),
            CarbonReportVarianceRequiredLabelRule("carbon report variance needs an entry"),
        ),
    )
}
