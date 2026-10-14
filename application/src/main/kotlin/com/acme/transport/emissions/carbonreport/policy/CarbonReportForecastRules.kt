package com.acme.transport.emissions.carbonreport.policy

/** A rule a carbon report forecast must pass. Check returns its problems, or none. */
interface CarbonReportForecastRule {
    fun check(item: CarbonReportForecast): MutableList<String>
}

data class CarbonReportForecastReviewerLengthRule(var maxLength: Int) : CarbonReportForecastRule {
    override fun check(item: CarbonReportForecast): MutableList<String> {
        val size = item.reviewer.length
        if (size > maxLength) {
            return mutableListOf<String>("reviewer is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CarbonReportForecastRequiredCodeRule(var message: String) : CarbonReportForecastRule {
    override fun check(item: CarbonReportForecast): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CarbonReportForecastRequiredLabelRule(var message: String) : CarbonReportForecastRule {
    override fun check(item: CarbonReportForecast): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a carbon report forecast. */
data class CarbonReportForecastValidator(var rules: MutableList<CarbonReportForecastRule>) {
    fun validate(item: CarbonReportForecast): MutableList<String> {
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

    fun requireValid(item: CarbonReportForecast) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CarbonReportForecast>): MutableMap<String, MutableList<String>> {
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

fun defaultCarbonReportForecastValidator(): CarbonReportForecastValidator {
    return CarbonReportForecastValidator(
        mutableListOf<CarbonReportForecastRule>(
            CarbonReportForecastReviewerLengthRule(32),
            CarbonReportForecastRequiredCodeRule("carbon report forecast needs this field"),
            CarbonReportForecastRequiredLabelRule("carbon report forecast needs this field"),
        ),
    )
}
