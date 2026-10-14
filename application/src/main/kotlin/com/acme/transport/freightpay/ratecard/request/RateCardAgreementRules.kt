package com.acme.transport.freightpay.ratecard.request

/** A rule a rate card agreement must pass. Check returns its problems, or none. */
interface RateCardAgreementRule {
    fun check(item: RateCardAgreement): MutableList<String>
}

data class RateCardAgreementFuelLitersRangeRule(var min: Double, var max: Double) : RateCardAgreementRule {
    override fun check(item: RateCardAgreement): MutableList<String> {
        val value = item.fuelLiters
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("fuel liters $value is below $min")
        }
        if (value > max) {
            problems.add("fuel liters $value is above $max")
        }
        return problems
    }
}

data class RateCardAgreementVersionRangeRule(var min: Int, var max: Int) : RateCardAgreementRule {
    override fun check(item: RateCardAgreement): MutableList<String> {
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

data class RateCardAgreementOwnerIdLengthRule(var maxLength: Int) : RateCardAgreementRule {
    override fun check(item: RateCardAgreement): MutableList<String> {
        val size = item.ownerId.length
        if (size > maxLength) {
            return mutableListOf<String>("owner id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a rate card agreement. */
data class RateCardAgreementValidator(var rules: MutableList<RateCardAgreementRule>) {
    fun validate(item: RateCardAgreement): MutableList<String> {
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

    fun requireValid(item: RateCardAgreement) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RateCardAgreement>): MutableMap<String, MutableList<String>> {
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

fun defaultRateCardAgreementValidator(): RateCardAgreementValidator {
    return RateCardAgreementValidator(
        mutableListOf<RateCardAgreementRule>(
            RateCardAgreementFuelLitersRangeRule(5.4, 1197.4),
            RateCardAgreementVersionRangeRule(64, 80),
            RateCardAgreementOwnerIdLengthRule(32),
        ),
    )
}
