package com.acme.transport.network.zone.forecast

/** A rule a zone forecast must pass. Check returns its problems, or none. */
interface ZoneForecastRule {
    fun check(item: ZoneForecast): MutableList<String>
}

data class ZoneForecastRequiredDepotRule(var message: String) : ZoneForecastRule {
    override fun check(item: ZoneForecast): MutableList<String> {
        if (item.depot.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ZoneForecastPalletsRangeRule(var min: Int, var max: Int) : ZoneForecastRule {
    override fun check(item: ZoneForecast): MutableList<String> {
        val value = item.pallets
        if (value < min || value > max) {
            return mutableListOf<String>("pallets $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class ZoneForecastCeilingRangeRule(var min: Double, var max: Double) : ZoneForecastRule {
    override fun check(item: ZoneForecast): MutableList<String> {
        val value = item.ceiling
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("ceiling $value is below $min")
        }
        if (value > max) {
            problems.add("ceiling $value is above $max")
        }
        return problems
    }
}

data class ZoneForecastRatioRangeRule(var min: Double, var max: Double) : ZoneForecastRule {
    override fun check(item: ZoneForecast): MutableList<String> {
        val value = item.ratio
        if (value < min || value > max) {
            return mutableListOf<String>("ratio $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class ZoneForecastRequiredLabelRule(var message: String) : ZoneForecastRule {
    override fun check(item: ZoneForecast): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ZoneForecastDepotLengthRule(var maxLength: Int) : ZoneForecastRule {
    override fun check(item: ZoneForecast): MutableList<String> {
        val size = item.depot.length
        if (size > maxLength) {
            return mutableListOf<String>("depot is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a zone forecast. */
data class ZoneForecastValidator(var rules: MutableList<ZoneForecastRule>) {
    fun validate(item: ZoneForecast): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ZoneForecast) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ZoneForecast>): MutableMap<String, MutableList<String>> {
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

fun defaultZoneForecastValidator(): ZoneForecastValidator {
    return ZoneForecastValidator(
        mutableListOf<ZoneForecastRule>(
            ZoneForecastRequiredDepotRule("zone forecast needs this field"),
            ZoneForecastPalletsRangeRule(16, 86400),
            ZoneForecastCeilingRangeRule(3.5, 2665.7),
            ZoneForecastRatioRangeRule(9.2, 3166.2),
            ZoneForecastRequiredLabelRule("zone forecast needs this field"),
            ZoneForecastDepotLengthRule(64),
        ),
    )
}
