package com.acme.transport.emissions.carbonreport.reportagreement

/** A rule a fallback carbon report agreement must pass. Check returns its problems, or none. */
interface FallbackCarbonReportAgreementRule {
    fun check(item: FallbackCarbonReportAgreement): MutableList<String>
}

data class FallbackCarbonReportAgreementCodeLengthRule(var maxLength: Int) : FallbackCarbonReportAgreementRule {
    override fun check(item: FallbackCarbonReportAgreement): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FallbackCarbonReportAgreementCodePrefixRule(var prefix: String) : FallbackCarbonReportAgreementRule {
    override fun check(item: FallbackCarbonReportAgreement): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class FallbackCarbonReportAgreementRateRangeRule(
    var min: Double,
    var max: Double,
) : FallbackCarbonReportAgreementRule {
    override fun check(item: FallbackCarbonReportAgreement): MutableList<String> {
        val value = item.rate
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("rate $value is below $min")
        }
        if (value > max) {
            problems.add("rate $value is above $max")
        }
        return problems
    }
}

data class FallbackCarbonReportAgreementTitleLengthRule(
    var maxLength: Int,
) : FallbackCarbonReportAgreementRule {
    override fun check(item: FallbackCarbonReportAgreement): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FallbackCarbonReportAgreementBudgetRangeRule(
    var min: Double,
    var max: Double,
) : FallbackCarbonReportAgreementRule {
    override fun check(item: FallbackCarbonReportAgreement): MutableList<String> {
        val value = item.budget
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("budget $value is below $min")
        }
        if (value > max) {
            problems.add("budget $value is above $max")
        }
        return problems
    }
}

data class FallbackCarbonReportAgreementRequiredCodeRule(
    var message: String,
) : FallbackCarbonReportAgreementRule {
    override fun check(item: FallbackCarbonReportAgreement): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a fallback carbon report agreement. */
data class FallbackCarbonReportAgreementValidator(var rules: MutableList<FallbackCarbonReportAgreementRule>) {
    fun validate(item: FallbackCarbonReportAgreement): MutableList<String> {
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

    fun requireValid(item: FallbackCarbonReportAgreement) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FallbackCarbonReportAgreement>): MutableMap<String, MutableList<String>> {
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

fun defaultFallbackCarbonReportAgreementValidator(): FallbackCarbonReportAgreementValidator {
    return FallbackCarbonReportAgreementValidator(
        mutableListOf<FallbackCarbonReportAgreementRule>(
            FallbackCarbonReportAgreementCodeLengthRule(255),
            FallbackCarbonReportAgreementCodePrefixRule("X"),
            FallbackCarbonReportAgreementRateRangeRule(8.1, 2349.2),
            FallbackCarbonReportAgreementTitleLengthRule(64),
            FallbackCarbonReportAgreementBudgetRangeRule(7.4, 1011.6),
            FallbackCarbonReportAgreementRequiredCodeRule("fallback carbon report agreement needs an entry"),
        ),
    )
}
