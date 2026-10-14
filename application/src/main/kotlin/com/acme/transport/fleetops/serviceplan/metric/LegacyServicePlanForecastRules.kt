package com.acme.transport.fleetops.serviceplan.metric

import com.acme.transport.kit.text.normalize

/** A rule a legacy service plan forecast must pass. Check returns its problems, or none. */
interface LegacyServicePlanForecastRule {
    fun check(item: LegacyServicePlanForecast): MutableList<String>
}

data class LegacyServicePlanForecastRequiredTrailerIdRule(var message: String) : LegacyServicePlanForecastRule {
    override fun check(item: LegacyServicePlanForecast): MutableList<String> {
        if (item.trailerId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LegacyServicePlanForecastCodeLengthRule(var maxLength: Int) : LegacyServicePlanForecastRule {
    override fun check(item: LegacyServicePlanForecast): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LegacyServicePlanForecastAllowedCodeRule(
    var allowed: MutableList<String>,
) : LegacyServicePlanForecastRule {
    override fun check(item: LegacyServicePlanForecast): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is inconsistent")
        }
        return mutableListOf<String>()
    }
}

data class LegacyServicePlanForecastTrailerIdLengthRule(var maxLength: Int) : LegacyServicePlanForecastRule {
    override fun check(item: LegacyServicePlanForecast): MutableList<String> {
        val size = item.trailerId.length
        if (size > maxLength) {
            return mutableListOf<String>("trailer id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LegacyServicePlanForecastActiveGuardRule(var limit: Double) : LegacyServicePlanForecastRule {
    override fun check(item: LegacyServicePlanForecast): MutableList<String> {
        if (item.active && item.floorValue > limit) {
            return mutableListOf<String>("floor value ${item.floorValue} exceeds $limit while active")
        }
        return mutableListOf<String>()
    }
}

data class LegacyServicePlanForecastFloorValueRangeRule(
    var min: Double,
    var max: Double,
) : LegacyServicePlanForecastRule {
    override fun check(item: LegacyServicePlanForecast): MutableList<String> {
        val value = item.floorValue
        if (value < min || value > max) {
            return mutableListOf<String>("floor value $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a legacy service plan forecast. */
data class LegacyServicePlanForecastValidator(var rules: MutableList<LegacyServicePlanForecastRule>) {
    fun validate(item: LegacyServicePlanForecast): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: LegacyServicePlanForecast) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LegacyServicePlanForecast>): MutableMap<String, MutableList<String>> {
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

fun defaultLegacyServicePlanForecastValidator(): LegacyServicePlanForecastValidator {
    return LegacyServicePlanForecastValidator(
        mutableListOf<LegacyServicePlanForecastRule>(
            LegacyServicePlanForecastRequiredTrailerIdRule("legacy service plan forecast needs an entry"),
            LegacyServicePlanForecastCodeLengthRule(120),
            LegacyServicePlanForecastAllowedCodeRule(mutableListOf<String>("weighted")),
            LegacyServicePlanForecastTrailerIdLengthRule(32),
            LegacyServicePlanForecastActiveGuardRule(7.0),
            LegacyServicePlanForecastFloorValueRangeRule(9.8, 1377.8),
        ),
    )
}
