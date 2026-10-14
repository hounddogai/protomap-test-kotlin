package com.acme.transport.freightpay.ratecard.cardbundle

import com.acme.transport.kit.text.normalize

/** A rule a lenient rate card forecast must pass. Check returns its problems, or none. */
interface LenientRateCardForecastRule {
    fun check(item: LenientRateCardForecast): MutableList<String>
}

data class LenientRateCardForecastRequiredTitleRule(var message: String) : LenientRateCardForecastRule {
    override fun check(item: LenientRateCardForecast): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LenientRateCardForecastDockLengthRule(var maxLength: Int) : LenientRateCardForecastRule {
    override fun check(item: LenientRateCardForecast): MutableList<String> {
        val size = item.dock.length
        if (size > maxLength) {
            return mutableListOf<String>("dock is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LenientRateCardForecastCodeLengthRule(var maxLength: Int) : LenientRateCardForecastRule {
    override fun check(item: LenientRateCardForecast): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LenientRateCardForecastAllowedCountryRule(
    var allowed: MutableList<String>,
) : LenientRateCardForecastRule {
    override fun check(item: LenientRateCardForecast): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.country))) {
            return mutableListOf<String>("country ${item.country} is not allowed")
        }
        return mutableListOf<String>()
    }
}

data class LenientRateCardForecastTitleLengthRule(var maxLength: Int) : LenientRateCardForecastRule {
    override fun check(item: LenientRateCardForecast): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a lenient rate card forecast. */
data class LenientRateCardForecastValidator(var rules: MutableList<LenientRateCardForecastRule>) {
    fun validate(item: LenientRateCardForecast): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: LenientRateCardForecast) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LenientRateCardForecast>): MutableMap<String, MutableList<String>> {
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

fun defaultLenientRateCardForecastValidator(): LenientRateCardForecastValidator {
    return LenientRateCardForecastValidator(
        mutableListOf<LenientRateCardForecastRule>(
            LenientRateCardForecastRequiredTitleRule("lenient rate card forecast needs this field"),
            LenientRateCardForecastDockLengthRule(64),
            LenientRateCardForecastCodeLengthRule(80),
            LenientRateCardForecastAllowedCountryRule(mutableListOf<String>()),
            LenientRateCardForecastTitleLengthRule(64),
        ),
    )
}
