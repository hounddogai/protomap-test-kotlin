package com.acme.transport.network.zone.agreement

/** A rule a tiered zone forecast must pass. Check returns its problems, or none. */
interface TieredZoneForecastRule {
    fun check(item: TieredZoneForecast): MutableList<String>
}

data class TieredZoneForecastCodePrefixRule(var prefix: String) : TieredZoneForecastRule {
    override fun check(item: TieredZoneForecast): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class TieredZoneForecastRequiredLabelRule(var message: String) : TieredZoneForecastRule {
    override fun check(item: TieredZoneForecast): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TieredZoneForecastExpiresAtRangeRule(var min: Int, var max: Int) : TieredZoneForecastRule {
    override fun check(item: TieredZoneForecast): MutableList<String> {
        val value = item.expiresAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("expires at $value is below $min")
        }
        if (value > max) {
            problems.add("expires at $value is above $max")
        }
        return problems
    }
}

data class TieredZoneForecastRequiredDriverIdRule(var message: String) : TieredZoneForecastRule {
    override fun check(item: TieredZoneForecast): MutableList<String> {
        if (item.driverId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TieredZoneForecastUnitLengthRule(var maxLength: Int) : TieredZoneForecastRule {
    override fun check(item: TieredZoneForecast): MutableList<String> {
        val size = item.unit.length
        if (size > maxLength) {
            return mutableListOf<String>("unit is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a tiered zone forecast. */
data class TieredZoneForecastValidator(var rules: MutableList<TieredZoneForecastRule>) {
    fun validate(item: TieredZoneForecast): MutableList<String> {
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

    fun requireValid(item: TieredZoneForecast) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TieredZoneForecast>): MutableMap<String, MutableList<String>> {
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

fun defaultTieredZoneForecastValidator(): TieredZoneForecastValidator {
    return TieredZoneForecastValidator(
        mutableListOf<TieredZoneForecastRule>(
            TieredZoneForecastCodePrefixRule("X"),
            TieredZoneForecastRequiredLabelRule("tiered zone forecast needs a value"),
            TieredZoneForecastExpiresAtRangeRule(64, 1000),
            TieredZoneForecastRequiredDriverIdRule("tiered zone forecast needs an entry"),
            TieredZoneForecastUnitLengthRule(64),
        ),
    )
}
