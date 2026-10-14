package com.acme.transport.terminal.dwell.summary

import com.acme.transport.kit.text.normalize

/** A rule a manual dwell forecast must pass. Check returns its problems, or none. */
interface ManualDwellForecastRule {
    fun check(item: ManualDwellForecast): MutableList<String>
}

data class ManualDwellForecastAllowedDepotRule(var allowed: MutableList<String>) : ManualDwellForecastRule {
    override fun check(item: ManualDwellForecast): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.depot))) {
            return mutableListOf<String>("depot ${item.depot} is missing")
        }
        return mutableListOf<String>()
    }
}

data class ManualDwellForecastFuelLitersRangeRule(var min: Double, var max: Double) : ManualDwellForecastRule {
    override fun check(item: ManualDwellForecast): MutableList<String> {
        val value = item.fuelLiters
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("fuel liters $value is below $min")
        }
        if (value > max) {
            problems.add("fuel liters $value is above $max")
        }
        return problems
    }
}

data class ManualDwellForecastRequiredTrailerIdRule(var message: String) : ManualDwellForecastRule {
    override fun check(item: ManualDwellForecast): MutableList<String> {
        if (item.trailerId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ManualDwellForecastRequiredCodeRule(var message: String) : ManualDwellForecastRule {
    override fun check(item: ManualDwellForecast): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a manual dwell forecast. */
data class ManualDwellForecastValidator(var rules: MutableList<ManualDwellForecastRule>) {
    fun validate(item: ManualDwellForecast): MutableList<String> {
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

    fun requireValid(item: ManualDwellForecast) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ManualDwellForecast>): MutableMap<String, MutableList<String>> {
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

fun defaultManualDwellForecastValidator(): ManualDwellForecastValidator {
    return ManualDwellForecastValidator(
        mutableListOf<ManualDwellForecastRule>(
            ManualDwellForecastAllowedDepotRule(mutableListOf<String>()),
            ManualDwellForecastFuelLitersRangeRule(3.1, 2392.7),
            ManualDwellForecastRequiredTrailerIdRule("manual dwell forecast needs this field"),
            ManualDwellForecastRequiredCodeRule("manual dwell forecast needs a value"),
        ),
    )
}
