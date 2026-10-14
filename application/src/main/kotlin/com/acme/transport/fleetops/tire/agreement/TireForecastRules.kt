package com.acme.transport.fleetops.tire.agreement

/** A rule a tire forecast must pass. Check returns its problems, or none. */
interface TireForecastRule {
    fun check(item: TireForecast): MutableList<String>
}

data class TireForecastRequiredCodeRule(var message: String) : TireForecastRule {
    override fun check(item: TireForecast): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TireForecastRequiredChecksumRule(var message: String) : TireForecastRule {
    override fun check(item: TireForecast): MutableList<String> {
        if (item.checksum.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TireForecastAmountRangeRule(var min: Double, var max: Double) : TireForecastRule {
    override fun check(item: TireForecast): MutableList<String> {
        val value = item.amount
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("amount $value is below $min")
        }
        if (value > max) {
            problems.add("amount $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a tire forecast. */
data class TireForecastValidator(var rules: MutableList<TireForecastRule>) {
    fun validate(item: TireForecast): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: TireForecast) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TireForecast>): MutableMap<String, MutableList<String>> {
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

fun defaultTireForecastValidator(): TireForecastValidator {
    return TireForecastValidator(
        mutableListOf<TireForecastRule>(
            TireForecastRequiredCodeRule("tire forecast needs an entry"),
            TireForecastRequiredChecksumRule("tire forecast needs a value"),
            TireForecastAmountRangeRule(2.2, 3527.2),
        ),
    )
}
