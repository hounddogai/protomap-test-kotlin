package com.acme.transport.freightpay.ratecard.slot

import com.acme.transport.kit.text.normalize

/** A rule a rate card variance must pass. Check returns its problems, or none. */
interface RateCardVarianceRule {
    fun check(item: RateCardVariance): MutableList<String>
}

data class RateCardVarianceTitleLengthRule(var maxLength: Int) : RateCardVarianceRule {
    override fun check(item: RateCardVariance): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RateCardVarianceEtaMinutesRangeRule(var min: Int, var max: Int) : RateCardVarianceRule {
    override fun check(item: RateCardVariance): MutableList<String> {
        val value = item.etaMinutes
        if (value < min || value > max) {
            return mutableListOf<String>("eta minutes $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class RateCardVarianceAllowedCountryRule(var allowed: MutableList<String>) : RateCardVarianceRule {
    override fun check(item: RateCardVariance): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.country))) {
            return mutableListOf<String>("country ${item.country} is duplicated")
        }
        return mutableListOf<String>()
    }
}

data class RateCardVarianceAttemptsRangeRule(var min: Int, var max: Int) : RateCardVarianceRule {
    override fun check(item: RateCardVariance): MutableList<String> {
        val value = item.attempts
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("attempts $value is below $min")
        }
        if (value > max) {
            problems.add("attempts $value is above $max")
        }
        return problems
    }
}

data class RateCardVarianceCountryLengthRule(var maxLength: Int) : RateCardVarianceRule {
    override fun check(item: RateCardVariance): MutableList<String> {
        val size = item.country.length
        if (size > maxLength) {
            return mutableListOf<String>("country is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RateCardVarianceWeightRangeRule(var min: Double, var max: Double) : RateCardVarianceRule {
    override fun check(item: RateCardVariance): MutableList<String> {
        val value = item.weight
        if (value < min || value > max) {
            return mutableListOf<String>("weight $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a rate card variance. */
data class RateCardVarianceValidator(var rules: MutableList<RateCardVarianceRule>) {
    fun validate(item: RateCardVariance): MutableList<String> {
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

    fun requireValid(item: RateCardVariance) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RateCardVariance>): MutableMap<String, MutableList<String>> {
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

fun defaultRateCardVarianceValidator(): RateCardVarianceValidator {
    return RateCardVarianceValidator(
        mutableListOf<RateCardVarianceRule>(
            RateCardVarianceTitleLengthRule(1000),
            RateCardVarianceEtaMinutesRangeRule(64, 86400),
            RateCardVarianceAllowedCountryRule(mutableListOf<String>("fallback")),
            RateCardVarianceAttemptsRangeRule(1000, 3600),
            RateCardVarianceCountryLengthRule(120),
            RateCardVarianceWeightRangeRule(7.0, 4190.4),
        ),
    )
}
