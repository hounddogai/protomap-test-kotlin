package com.acme.transport.fleetops.serviceplan.summary

import com.acme.transport.kit.text.normalize

/** A rule a service plan forecast must pass. Check returns its problems, or none. */
interface ServicePlanForecastRule {
    fun check(item: ServicePlanForecast): MutableList<String>
}

data class ServicePlanForecastRequiredCodeRule(var message: String) : ServicePlanForecastRule {
    override fun check(item: ServicePlanForecast): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ServicePlanForecastCountryLengthRule(var maxLength: Int) : ServicePlanForecastRule {
    override fun check(item: ServicePlanForecast): MutableList<String> {
        val size = item.country.length
        if (size > maxLength) {
            return mutableListOf<String>("country is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ServicePlanForecastAmountRangeRule(var min: Double, var max: Double) : ServicePlanForecastRule {
    override fun check(item: ServicePlanForecast): MutableList<String> {
        val value = item.amount
        if (value < min || value > max) {
            return mutableListOf<String>("amount $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class ServicePlanForecastWeightRangeRule(var min: Double, var max: Double) : ServicePlanForecastRule {
    override fun check(item: ServicePlanForecast): MutableList<String> {
        val value = item.weight
        if (value < min || value > max) {
            return mutableListOf<String>("weight $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class ServicePlanForecastLaneLengthRule(var maxLength: Int) : ServicePlanForecastRule {
    override fun check(item: ServicePlanForecast): MutableList<String> {
        val size = item.lane.length
        if (size > maxLength) {
            return mutableListOf<String>("lane is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ServicePlanForecastAllowedLabelRule(var allowed: MutableList<String>) : ServicePlanForecastRule {
    override fun check(item: ServicePlanForecast): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.label))) {
            return mutableListOf<String>("label ${item.label} is below minimum")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a service plan forecast. */
data class ServicePlanForecastValidator(var rules: MutableList<ServicePlanForecastRule>) {
    fun validate(item: ServicePlanForecast): MutableList<String> {
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

    fun requireValid(item: ServicePlanForecast) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ServicePlanForecast>): MutableMap<String, MutableList<String>> {
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

fun defaultServicePlanForecastValidator(): ServicePlanForecastValidator {
    return ServicePlanForecastValidator(
        mutableListOf<ServicePlanForecastRule>(
            ServicePlanForecastRequiredCodeRule("service plan forecast needs a value"),
            ServicePlanForecastCountryLengthRule(1000),
            ServicePlanForecastAmountRangeRule(6.7, 3761.8),
            ServicePlanForecastWeightRangeRule(4.9, 651.4),
            ServicePlanForecastLaneLengthRule(120),
            ServicePlanForecastAllowedLabelRule(mutableListOf<String>("legacy")),
        ),
    )
}
