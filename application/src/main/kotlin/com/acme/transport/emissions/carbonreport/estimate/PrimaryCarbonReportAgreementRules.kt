package com.acme.transport.emissions.carbonreport.estimate

/** A rule a primary carbon report agreement must pass. Check returns its problems, or none. */
interface PrimaryCarbonReportAgreementRule {
    fun check(item: PrimaryCarbonReportAgreement): MutableList<String>
}

data class PrimaryCarbonReportAgreementRequiredCodeRule(
    var message: String,
) : PrimaryCarbonReportAgreementRule {
    override fun check(item: PrimaryCarbonReportAgreement): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PrimaryCarbonReportAgreementDepotLengthRule(var maxLength: Int) : PrimaryCarbonReportAgreementRule {
    override fun check(item: PrimaryCarbonReportAgreement): MutableList<String> {
        val size = item.depot.length
        if (size > maxLength) {
            return mutableListOf<String>("depot is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryCarbonReportAgreementGraceDaysRangeRule(
    var min: Int,
    var max: Int,
) : PrimaryCarbonReportAgreementRule {
    override fun check(item: PrimaryCarbonReportAgreement): MutableList<String> {
        val value = item.graceDays
        if (value < min || value > max) {
            return mutableListOf<String>("grace days $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryCarbonReportAgreementRequiredCurrencyRule(
    var message: String,
) : PrimaryCarbonReportAgreementRule {
    override fun check(item: PrimaryCarbonReportAgreement): MutableList<String> {
        if (item.currency.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a primary carbon report agreement. */
data class PrimaryCarbonReportAgreementValidator(var rules: MutableList<PrimaryCarbonReportAgreementRule>) {
    fun validate(item: PrimaryCarbonReportAgreement): MutableList<String> {
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

    fun requireValid(item: PrimaryCarbonReportAgreement) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PrimaryCarbonReportAgreement>): MutableMap<String, MutableList<String>> {
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

fun defaultPrimaryCarbonReportAgreementValidator(): PrimaryCarbonReportAgreementValidator {
    return PrimaryCarbonReportAgreementValidator(
        mutableListOf<PrimaryCarbonReportAgreementRule>(
            PrimaryCarbonReportAgreementRequiredCodeRule("primary carbon report agreement needs this field"),
            PrimaryCarbonReportAgreementDepotLengthRule(64),
            PrimaryCarbonReportAgreementGraceDaysRangeRule(80, 255),
            PrimaryCarbonReportAgreementRequiredCurrencyRule("primary carbon report agreement needs a value"),
        ),
    )
}
