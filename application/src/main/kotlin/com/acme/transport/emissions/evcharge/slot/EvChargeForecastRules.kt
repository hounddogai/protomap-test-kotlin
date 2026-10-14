package com.acme.transport.emissions.evcharge.slot

/** A rule a ev charge forecast must pass. Check returns its problems, or none. */
interface EvChargeForecastRule {
    fun check(item: EvChargeForecast): MutableList<String>
}

data class EvChargeForecastRequiredCountryRule(var message: String) : EvChargeForecastRule {
    override fun check(item: EvChargeForecast): MutableList<String> {
        if (item.country.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class EvChargeForecastRequiredTitleRule(var message: String) : EvChargeForecastRule {
    override fun check(item: EvChargeForecast): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class EvChargeForecastAmountRangeRule(var min: Double, var max: Double) : EvChargeForecastRule {
    override fun check(item: EvChargeForecast): MutableList<String> {
        val value = item.amount
        if (value < min || value > max) {
            return mutableListOf<String>("amount $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class EvChargeForecastCodePrefixRule(var prefix: String) : EvChargeForecastRule {
    override fun check(item: EvChargeForecast): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class EvChargeForecastTitleLengthRule(var maxLength: Int) : EvChargeForecastRule {
    override fun check(item: EvChargeForecast): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a ev charge forecast. */
data class EvChargeForecastValidator(var rules: MutableList<EvChargeForecastRule>) {
    fun validate(item: EvChargeForecast): MutableList<String> {
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

    fun requireValid(item: EvChargeForecast) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<EvChargeForecast>): MutableMap<String, MutableList<String>> {
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

fun defaultEvChargeForecastValidator(): EvChargeForecastValidator {
    return EvChargeForecastValidator(
        mutableListOf<EvChargeForecastRule>(
            EvChargeForecastRequiredCountryRule("ev charge forecast needs a value"),
            EvChargeForecastRequiredTitleRule("ev charge forecast needs a value"),
            EvChargeForecastAmountRangeRule(4.6, 931.9),
            EvChargeForecastCodePrefixRule("REF"),
            EvChargeForecastTitleLengthRule(16),
        ),
    )
}
