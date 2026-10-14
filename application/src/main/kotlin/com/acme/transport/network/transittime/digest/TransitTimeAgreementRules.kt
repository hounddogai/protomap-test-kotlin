package com.acme.transport.network.transittime.digest

/** A rule a transit time agreement must pass. Check returns its problems, or none. */
interface TransitTimeAgreementRule {
    fun check(item: TransitTimeAgreement): MutableList<String>
}

data class TransitTimeAgreementRequiredCategoryRule(var message: String) : TransitTimeAgreementRule {
    override fun check(item: TransitTimeAgreement): MutableList<String> {
        if (item.category.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TransitTimeAgreementDescriptionLengthRule(var maxLength: Int) : TransitTimeAgreementRule {
    override fun check(item: TransitTimeAgreement): MutableList<String> {
        val size = item.description.length
        if (size > maxLength) {
            return mutableListOf<String>("description is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TransitTimeAgreementFuelLitersRangeRule(
    var min: Double,
    var max: Double,
) : TransitTimeAgreementRule {
    override fun check(item: TransitTimeAgreement): MutableList<String> {
        val value = item.fuelLiters
        if (value < min || value > max) {
            return mutableListOf<String>("fuel liters $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a transit time agreement. */
data class TransitTimeAgreementValidator(var rules: MutableList<TransitTimeAgreementRule>) {
    fun validate(item: TransitTimeAgreement): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: TransitTimeAgreement) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TransitTimeAgreement>): MutableMap<String, MutableList<String>> {
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

fun defaultTransitTimeAgreementValidator(): TransitTimeAgreementValidator {
    return TransitTimeAgreementValidator(
        mutableListOf<TransitTimeAgreementRule>(
            TransitTimeAgreementRequiredCategoryRule("transit time agreement needs an entry"),
            TransitTimeAgreementDescriptionLengthRule(86400),
            TransitTimeAgreementFuelLitersRangeRule(6.5, 2147.0),
        ),
    )
}
