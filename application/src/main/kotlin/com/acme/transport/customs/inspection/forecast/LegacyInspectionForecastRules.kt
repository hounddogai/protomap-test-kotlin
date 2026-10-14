package com.acme.transport.customs.inspection.forecast

/** A rule a legacy inspection forecast must pass. Check returns its problems, or none. */
interface LegacyInspectionForecastRule {
    fun check(item: LegacyInspectionForecast): MutableList<String>
}

data class LegacyInspectionForecastEtaMinutesRangeRule(
    var min: Int,
    var max: Int,
) : LegacyInspectionForecastRule {
    override fun check(item: LegacyInspectionForecast): MutableList<String> {
        val value = item.etaMinutes
        if (value < min || value > max) {
            return mutableListOf<String>("eta minutes $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class LegacyInspectionForecastRequiredCodeRule(var message: String) : LegacyInspectionForecastRule {
    override fun check(item: LegacyInspectionForecast): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LegacyInspectionForecastDurationMinutesRangeRule(
    var min: Int,
    var max: Int,
) : LegacyInspectionForecastRule {
    override fun check(item: LegacyInspectionForecast): MutableList<String> {
        val value = item.durationMinutes
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("duration minutes $value is below $min")
        }
        if (value > max) {
            problems.add("duration minutes $value is above $max")
        }
        return problems
    }
}

data class LegacyInspectionForecastCountryLengthRule(var maxLength: Int) : LegacyInspectionForecastRule {
    override fun check(item: LegacyInspectionForecast): MutableList<String> {
        val size = item.country.length
        if (size > maxLength) {
            return mutableListOf<String>("country is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LegacyInspectionForecastRequiredLabelRule(var message: String) : LegacyInspectionForecastRule {
    override fun check(item: LegacyInspectionForecast): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LegacyInspectionForecastPalletsRangeRule(var min: Int, var max: Int) : LegacyInspectionForecastRule {
    override fun check(item: LegacyInspectionForecast): MutableList<String> {
        val value = item.pallets
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("pallets $value is below $min")
        }
        if (value > max) {
            problems.add("pallets $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a legacy inspection forecast. */
data class LegacyInspectionForecastValidator(var rules: MutableList<LegacyInspectionForecastRule>) {
    fun validate(item: LegacyInspectionForecast): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: LegacyInspectionForecast) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LegacyInspectionForecast>): MutableMap<String, MutableList<String>> {
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

fun defaultLegacyInspectionForecastValidator(): LegacyInspectionForecastValidator {
    return LegacyInspectionForecastValidator(
        mutableListOf<LegacyInspectionForecastRule>(
            LegacyInspectionForecastEtaMinutesRangeRule(64, 3600),
            LegacyInspectionForecastRequiredCodeRule("legacy inspection forecast needs an entry"),
            LegacyInspectionForecastDurationMinutesRangeRule(86400, 86400),
            LegacyInspectionForecastCountryLengthRule(80),
            LegacyInspectionForecastRequiredLabelRule("legacy inspection forecast needs a value"),
            LegacyInspectionForecastPalletsRangeRule(80, 80),
        ),
    )
}
