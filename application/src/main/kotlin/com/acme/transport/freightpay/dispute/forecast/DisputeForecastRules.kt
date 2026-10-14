package com.acme.transport.freightpay.dispute.forecast

import com.acme.transport.kit.text.normalize

/** A rule a dispute forecast must pass. Check returns its problems, or none. */
interface DisputeForecastRule {
    fun check(item: DisputeForecast): MutableList<String>
}

data class DisputeForecastCodePrefixRule(var prefix: String) : DisputeForecastRule {
    override fun check(item: DisputeForecast): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class DisputeForecastLaneLengthRule(var maxLength: Int) : DisputeForecastRule {
    override fun check(item: DisputeForecast): MutableList<String> {
        val size = item.lane.length
        if (size > maxLength) {
            return mutableListOf<String>("lane is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DisputeForecastRequiredTrailerIdRule(var message: String) : DisputeForecastRule {
    override fun check(item: DisputeForecast): MutableList<String> {
        if (item.trailerId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DisputeForecastAllowedNameRule(var allowed: MutableList<String>) : DisputeForecastRule {
    override fun check(item: DisputeForecast): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.name))) {
            return mutableListOf<String>("name ${item.name} is not allowed")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a dispute forecast. */
data class DisputeForecastValidator(var rules: MutableList<DisputeForecastRule>) {
    fun validate(item: DisputeForecast): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: DisputeForecast) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DisputeForecast>): MutableMap<String, MutableList<String>> {
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

fun defaultDisputeForecastValidator(): DisputeForecastValidator {
    return DisputeForecastValidator(
        mutableListOf<DisputeForecastRule>(
            DisputeForecastCodePrefixRule("OPS"),
            DisputeForecastLaneLengthRule(255),
            DisputeForecastRequiredTrailerIdRule("dispute forecast needs a value"),
            DisputeForecastAllowedNameRule(mutableListOf<String>("global", "batched", "capped")),
        ),
    )
}
