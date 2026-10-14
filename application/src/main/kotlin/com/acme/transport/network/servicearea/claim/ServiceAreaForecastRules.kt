package com.acme.transport.network.servicearea.claim

import com.acme.transport.kit.text.normalize

/** A rule a service area forecast must pass. Check returns its problems, or none. */
interface ServiceAreaForecastRule {
    fun check(item: ServiceAreaForecast): MutableList<String>
}

data class ServiceAreaForecastRequiredNameRule(var message: String) : ServiceAreaForecastRule {
    override fun check(item: ServiceAreaForecast): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ServiceAreaForecastAllowedCodeRule(var allowed: MutableList<String>) : ServiceAreaForecastRule {
    override fun check(item: ServiceAreaForecast): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is too long")
        }
        return mutableListOf<String>()
    }
}

data class ServiceAreaForecastNameLengthRule(var maxLength: Int) : ServiceAreaForecastRule {
    override fun check(item: ServiceAreaForecast): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ServiceAreaForecastDriverIdLengthRule(var maxLength: Int) : ServiceAreaForecastRule {
    override fun check(item: ServiceAreaForecast): MutableList<String> {
        val size = item.driverId.length
        if (size > maxLength) {
            return mutableListOf<String>("driver id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ServiceAreaForecastRequiredDriverIdRule(var message: String) : ServiceAreaForecastRule {
    override fun check(item: ServiceAreaForecast): MutableList<String> {
        if (item.driverId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ServiceAreaForecastRequiredCodeRule(var message: String) : ServiceAreaForecastRule {
    override fun check(item: ServiceAreaForecast): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a service area forecast. */
data class ServiceAreaForecastValidator(var rules: MutableList<ServiceAreaForecastRule>) {
    fun validate(item: ServiceAreaForecast): MutableList<String> {
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

    fun requireValid(item: ServiceAreaForecast) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ServiceAreaForecast>): MutableMap<String, MutableList<String>> {
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

fun defaultServiceAreaForecastValidator(): ServiceAreaForecastValidator {
    return ServiceAreaForecastValidator(
        mutableListOf<ServiceAreaForecastRule>(
            ServiceAreaForecastRequiredNameRule("service area forecast needs an entry"),
            ServiceAreaForecastAllowedCodeRule(mutableListOf<String>("manual", "strict")),
            ServiceAreaForecastNameLengthRule(120),
            ServiceAreaForecastDriverIdLengthRule(1000),
            ServiceAreaForecastRequiredDriverIdRule("service area forecast needs a value"),
            ServiceAreaForecastRequiredCodeRule("service area forecast needs an entry"),
        ),
    )
}
