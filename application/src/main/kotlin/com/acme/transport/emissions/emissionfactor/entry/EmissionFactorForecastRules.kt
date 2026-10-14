package com.acme.transport.emissions.emissionfactor.entry

import com.acme.transport.kit.text.normalize

/** A rule a emission factor forecast must pass. Check returns its problems, or none. */
interface EmissionFactorForecastRule {
    fun check(item: EmissionFactorForecast): MutableList<String>
}

data class EmissionFactorForecastAllowedChecksumRule(
    var allowed: MutableList<String>,
) : EmissionFactorForecastRule {
    override fun check(item: EmissionFactorForecast): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.checksum))) {
            return mutableListOf<String>("checksum ${item.checksum} is too long")
        }
        return mutableListOf<String>()
    }
}

data class EmissionFactorForecastDistanceKmRangeRule(
    var min: Double,
    var max: Double,
) : EmissionFactorForecastRule {
    override fun check(item: EmissionFactorForecast): MutableList<String> {
        val value = item.distanceKm
        if (value < min || value > max) {
            return mutableListOf<String>("distance km $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class EmissionFactorForecastAttemptsRangeRule(var min: Int, var max: Int) : EmissionFactorForecastRule {
    override fun check(item: EmissionFactorForecast): MutableList<String> {
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

data class EmissionFactorForecastRequiredChecksumRule(var message: String) : EmissionFactorForecastRule {
    override fun check(item: EmissionFactorForecast): MutableList<String> {
        if (item.checksum.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class EmissionFactorForecastLabelLengthRule(var maxLength: Int) : EmissionFactorForecastRule {
    override fun check(item: EmissionFactorForecast): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class EmissionFactorForecastChecksumLengthRule(var maxLength: Int) : EmissionFactorForecastRule {
    override fun check(item: EmissionFactorForecast): MutableList<String> {
        val size = item.checksum.length
        if (size > maxLength) {
            return mutableListOf<String>("checksum is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a emission factor forecast. */
data class EmissionFactorForecastValidator(var rules: MutableList<EmissionFactorForecastRule>) {
    fun validate(item: EmissionFactorForecast): MutableList<String> {
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

    fun requireValid(item: EmissionFactorForecast) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<EmissionFactorForecast>): MutableMap<String, MutableList<String>> {
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

fun defaultEmissionFactorForecastValidator(): EmissionFactorForecastValidator {
    return EmissionFactorForecastValidator(
        mutableListOf<EmissionFactorForecastRule>(
            EmissionFactorForecastAllowedChecksumRule(mutableListOf<String>()),
            EmissionFactorForecastDistanceKmRangeRule(6.0, 2879.9),
            EmissionFactorForecastAttemptsRangeRule(80, 120),
            EmissionFactorForecastRequiredChecksumRule("emission factor forecast needs a value"),
            EmissionFactorForecastLabelLengthRule(1000),
            EmissionFactorForecastChecksumLengthRule(86400),
        ),
    )
}
