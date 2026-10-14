package com.acme.transport.customs.hold.rollup

/** A rule a hold forecast must pass. Check returns its problems, or none. */
interface HoldForecastRule {
    fun check(item: HoldForecast): MutableList<String>
}

data class HoldForecastExternalRefLengthRule(var maxLength: Int) : HoldForecastRule {
    override fun check(item: HoldForecast): MutableList<String> {
        val size = item.externalRef.length
        if (size > maxLength) {
            return mutableListOf<String>("external ref is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class HoldForecastCurrencyLengthRule(var maxLength: Int) : HoldForecastRule {
    override fun check(item: HoldForecast): MutableList<String> {
        val size = item.currency.length
        if (size > maxLength) {
            return mutableListOf<String>("currency is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class HoldForecastRequiredCodeRule(var message: String) : HoldForecastRule {
    override fun check(item: HoldForecast): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a hold forecast. */
data class HoldForecastValidator(var rules: MutableList<HoldForecastRule>) {
    fun validate(item: HoldForecast): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: HoldForecast) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<HoldForecast>): MutableMap<String, MutableList<String>> {
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

fun defaultHoldForecastValidator(): HoldForecastValidator {
    return HoldForecastValidator(
        mutableListOf<HoldForecastRule>(
            HoldForecastExternalRefLengthRule(80),
            HoldForecastCurrencyLengthRule(16),
            HoldForecastRequiredCodeRule("hold forecast needs a value"),
        ),
    )
}
