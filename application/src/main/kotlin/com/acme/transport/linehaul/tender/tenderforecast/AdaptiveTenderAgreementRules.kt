package com.acme.transport.linehaul.tender.tenderforecast

/** A rule a adaptive tender agreement must pass. Check returns its problems, or none. */
interface AdaptiveTenderAgreementRule {
    fun check(item: AdaptiveTenderAgreement): MutableList<String>
}

data class AdaptiveTenderAgreementRequiredCodeRule(var message: String) : AdaptiveTenderAgreementRule {
    override fun check(item: AdaptiveTenderAgreement): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveTenderAgreementCodeLengthRule(var maxLength: Int) : AdaptiveTenderAgreementRule {
    override fun check(item: AdaptiveTenderAgreement): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveTenderAgreementDistanceKmRangeRule(
    var min: Double,
    var max: Double,
) : AdaptiveTenderAgreementRule {
    override fun check(item: AdaptiveTenderAgreement): MutableList<String> {
        val value = item.distanceKm
        if (value < min || value > max) {
            return mutableListOf<String>("distance km $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a adaptive tender agreement. */
data class AdaptiveTenderAgreementValidator(var rules: MutableList<AdaptiveTenderAgreementRule>) {
    fun validate(item: AdaptiveTenderAgreement): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: AdaptiveTenderAgreement) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AdaptiveTenderAgreement>): MutableMap<String, MutableList<String>> {
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

fun defaultAdaptiveTenderAgreementValidator(): AdaptiveTenderAgreementValidator {
    return AdaptiveTenderAgreementValidator(
        mutableListOf<AdaptiveTenderAgreementRule>(
            AdaptiveTenderAgreementRequiredCodeRule("adaptive tender agreement needs an entry"),
            AdaptiveTenderAgreementCodeLengthRule(32),
            AdaptiveTenderAgreementDistanceKmRangeRule(3.5, 272.0),
        ),
    )
}
