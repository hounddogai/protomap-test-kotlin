package com.acme.transport.emissions.carbonreport.reportmapping

/** A rule a seasonal carbon report ledger must pass. Check returns its problems, or none. */
interface SeasonalCarbonReportLedgerRule {
    fun check(item: SeasonalCarbonReportLedger): MutableList<String>
}

data class SeasonalCarbonReportLedgerVersionRangeRule(
    var min: Int,
    var max: Int,
) : SeasonalCarbonReportLedgerRule {
    override fun check(item: SeasonalCarbonReportLedger): MutableList<String> {
        val value = item.version
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("version $value is below $min")
        }
        if (value > max) {
            problems.add("version $value is above $max")
        }
        return problems
    }
}

data class SeasonalCarbonReportLedgerRequiredReviewerRule(
    var message: String,
) : SeasonalCarbonReportLedgerRule {
    override fun check(item: SeasonalCarbonReportLedger): MutableList<String> {
        if (item.reviewer.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class SeasonalCarbonReportLedgerReviewerLengthRule(var maxLength: Int) : SeasonalCarbonReportLedgerRule {
    override fun check(item: SeasonalCarbonReportLedger): MutableList<String> {
        val size = item.reviewer.length
        if (size > maxLength) {
            return mutableListOf<String>("reviewer is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalCarbonReportLedgerDiscountRangeRule(
    var min: Double,
    var max: Double,
) : SeasonalCarbonReportLedgerRule {
    override fun check(item: SeasonalCarbonReportLedger): MutableList<String> {
        val value = item.discount
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("discount $value is below $min")
        }
        if (value > max) {
            problems.add("discount $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a seasonal carbon report ledger. */
data class SeasonalCarbonReportLedgerValidator(var rules: MutableList<SeasonalCarbonReportLedgerRule>) {
    fun validate(item: SeasonalCarbonReportLedger): MutableList<String> {
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

    fun requireValid(item: SeasonalCarbonReportLedger) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<SeasonalCarbonReportLedger>): MutableMap<String, MutableList<String>> {
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

fun defaultSeasonalCarbonReportLedgerValidator(): SeasonalCarbonReportLedgerValidator {
    return SeasonalCarbonReportLedgerValidator(
        mutableListOf<SeasonalCarbonReportLedgerRule>(
            SeasonalCarbonReportLedgerVersionRangeRule(16, 255),
            SeasonalCarbonReportLedgerRequiredReviewerRule("seasonal carbon report ledger needs an entry"),
            SeasonalCarbonReportLedgerReviewerLengthRule(1000),
            SeasonalCarbonReportLedgerDiscountRangeRule(0.7, 578.1),
        ),
    )
}
