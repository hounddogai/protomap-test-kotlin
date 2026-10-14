package com.acme.transport.linehaul.tender.forecast

import com.acme.transport.kit.text.normalize

/** A rule a pending tender forecast must pass. Check returns its problems, or none. */
interface PendingTenderForecastRule {
    fun check(item: PendingTenderForecast): MutableList<String>
}

data class PendingTenderForecastAllowedCountryRule(
    var allowed: MutableList<String>,
) : PendingTenderForecastRule {
    override fun check(item: PendingTenderForecast): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.country))) {
            return mutableListOf<String>("country ${item.country} is expired")
        }
        return mutableListOf<String>()
    }
}

data class PendingTenderForecastRequiredCodeRule(var message: String) : PendingTenderForecastRule {
    override fun check(item: PendingTenderForecast): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PendingTenderForecastShiftCodeLengthRule(var maxLength: Int) : PendingTenderForecastRule {
    override fun check(item: PendingTenderForecast): MutableList<String> {
        val size = item.shiftCode.length
        if (size > maxLength) {
            return mutableListOf<String>("shift code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PendingTenderForecastCountryLengthRule(var maxLength: Int) : PendingTenderForecastRule {
    override fun check(item: PendingTenderForecast): MutableList<String> {
        val size = item.country.length
        if (size > maxLength) {
            return mutableListOf<String>("country is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PendingTenderForecastRetriesRangeRule(var min: Int, var max: Int) : PendingTenderForecastRule {
    override fun check(item: PendingTenderForecast): MutableList<String> {
        val value = item.retries
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("retries $value is below $min")
        }
        if (value > max) {
            problems.add("retries $value is above $max")
        }
        return problems
    }
}

data class PendingTenderForecastRequiredNameRule(var message: String) : PendingTenderForecastRule {
    override fun check(item: PendingTenderForecast): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a pending tender forecast. */
data class PendingTenderForecastValidator(var rules: MutableList<PendingTenderForecastRule>) {
    fun validate(item: PendingTenderForecast): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: PendingTenderForecast) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PendingTenderForecast>): MutableMap<String, MutableList<String>> {
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

fun defaultPendingTenderForecastValidator(): PendingTenderForecastValidator {
    return PendingTenderForecastValidator(
        mutableListOf<PendingTenderForecastRule>(
            PendingTenderForecastAllowedCountryRule(mutableListOf<String>("lenient")),
            PendingTenderForecastRequiredCodeRule("pending tender forecast needs an entry"),
            PendingTenderForecastShiftCodeLengthRule(120),
            PendingTenderForecastCountryLengthRule(64),
            PendingTenderForecastRetriesRangeRule(64, 80),
            PendingTenderForecastRequiredNameRule("pending tender forecast needs a value"),
        ),
    )
}
