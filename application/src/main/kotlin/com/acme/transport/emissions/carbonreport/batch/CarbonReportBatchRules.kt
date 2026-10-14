package com.acme.transport.emissions.carbonreport.batch

/** A rule a carbon report batch must pass. Check returns its problems, or none. */
interface CarbonReportBatchRule {
    fun check(item: CarbonReportBatch): MutableList<String>
}

data class CarbonReportBatchRequiredLabelRule(var message: String) : CarbonReportBatchRule {
    override fun check(item: CarbonReportBatch): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CarbonReportBatchCodeLengthRule(var maxLength: Int) : CarbonReportBatchRule {
    override fun check(item: CarbonReportBatch): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CarbonReportBatchRequiredCountryRule(var message: String) : CarbonReportBatchRule {
    override fun check(item: CarbonReportBatch): MutableList<String> {
        if (item.country.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a carbon report batch. */
data class CarbonReportBatchValidator(var rules: MutableList<CarbonReportBatchRule>) {
    fun validate(item: CarbonReportBatch): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: CarbonReportBatch) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CarbonReportBatch>): MutableMap<String, MutableList<String>> {
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

fun defaultCarbonReportBatchValidator(): CarbonReportBatchValidator {
    return CarbonReportBatchValidator(
        mutableListOf<CarbonReportBatchRule>(
            CarbonReportBatchRequiredLabelRule("carbon report batch needs a value"),
            CarbonReportBatchCodeLengthRule(16),
            CarbonReportBatchRequiredCountryRule("carbon report batch needs this field"),
        ),
    )
}
