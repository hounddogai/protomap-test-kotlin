package com.acme.transport.emissions.carbonreport.entry

/** A rule a seasonal carbon report agreement must pass. Check returns its problems, or none. */
interface SeasonalCarbonReportAgreementRule {
    fun check(item: SeasonalCarbonReportAgreement): MutableList<String>
}

data class SeasonalCarbonReportAgreementCodeLengthRule(var maxLength: Int) : SeasonalCarbonReportAgreementRule {
    override fun check(item: SeasonalCarbonReportAgreement): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalCarbonReportAgreementCurrencyLengthRule(
    var maxLength: Int,
) : SeasonalCarbonReportAgreementRule {
    override fun check(item: SeasonalCarbonReportAgreement): MutableList<String> {
        val size = item.currency.length
        if (size > maxLength) {
            return mutableListOf<String>("currency is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalCarbonReportAgreementFuelLitersRangeRule(
    var min: Double,
    var max: Double,
) : SeasonalCarbonReportAgreementRule {
    override fun check(item: SeasonalCarbonReportAgreement): MutableList<String> {
        val value = item.fuelLiters
        if (value < min || value > max) {
            return mutableListOf<String>("fuel liters $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a seasonal carbon report agreement. */
data class SeasonalCarbonReportAgreementValidator(var rules: MutableList<SeasonalCarbonReportAgreementRule>) {
    fun validate(item: SeasonalCarbonReportAgreement): MutableList<String> {
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

    fun requireValid(item: SeasonalCarbonReportAgreement) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<SeasonalCarbonReportAgreement>): MutableMap<String, MutableList<String>> {
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

fun defaultSeasonalCarbonReportAgreementValidator(): SeasonalCarbonReportAgreementValidator {
    return SeasonalCarbonReportAgreementValidator(
        mutableListOf<SeasonalCarbonReportAgreementRule>(
            SeasonalCarbonReportAgreementCodeLengthRule(80),
            SeasonalCarbonReportAgreementCurrencyLengthRule(64),
            SeasonalCarbonReportAgreementFuelLitersRangeRule(8.8, 1590.3),
        ),
    )
}
