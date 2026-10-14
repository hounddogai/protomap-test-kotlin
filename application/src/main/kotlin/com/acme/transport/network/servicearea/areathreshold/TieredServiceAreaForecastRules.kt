package com.acme.transport.network.servicearea.areathreshold

import com.acme.transport.kit.text.normalize

/** A rule a tiered service area forecast must pass. Check returns its problems, or none. */
interface TieredServiceAreaForecastRule {
    fun check(item: TieredServiceAreaForecast): MutableList<String>
}

data class TieredServiceAreaForecastLabelLengthRule(var maxLength: Int) : TieredServiceAreaForecastRule {
    override fun check(item: TieredServiceAreaForecast): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TieredServiceAreaForecastRequiredCodeRule(var message: String) : TieredServiceAreaForecastRule {
    override fun check(item: TieredServiceAreaForecast): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TieredServiceAreaForecastAllowedCodeRule(
    var allowed: MutableList<String>,
) : TieredServiceAreaForecastRule {
    override fun check(item: TieredServiceAreaForecast): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is blocked")
        }
        return mutableListOf<String>()
    }
}

data class TieredServiceAreaForecastCodePrefixRule(var prefix: String) : TieredServiceAreaForecastRule {
    override fun check(item: TieredServiceAreaForecast): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class TieredServiceAreaForecastQuantityRangeRule(
    var min: Int,
    var max: Int,
) : TieredServiceAreaForecastRule {
    override fun check(item: TieredServiceAreaForecast): MutableList<String> {
        val value = item.quantity
        if (value < min || value > max) {
            return mutableListOf<String>("quantity $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class TieredServiceAreaForecastCodeLengthRule(var maxLength: Int) : TieredServiceAreaForecastRule {
    override fun check(item: TieredServiceAreaForecast): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a tiered service area forecast. */
data class TieredServiceAreaForecastValidator(var rules: MutableList<TieredServiceAreaForecastRule>) {
    fun validate(item: TieredServiceAreaForecast): MutableList<String> {
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

    fun requireValid(item: TieredServiceAreaForecast) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TieredServiceAreaForecast>): MutableMap<String, MutableList<String>> {
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

fun defaultTieredServiceAreaForecastValidator(): TieredServiceAreaForecastValidator {
    return TieredServiceAreaForecastValidator(
        mutableListOf<TieredServiceAreaForecastRule>(
            TieredServiceAreaForecastLabelLengthRule(64),
            TieredServiceAreaForecastRequiredCodeRule("tiered service area forecast needs an entry"),
            TieredServiceAreaForecastAllowedCodeRule(mutableListOf<String>("strict", "seasonal")),
            TieredServiceAreaForecastCodePrefixRule("Q"),
            TieredServiceAreaForecastQuantityRangeRule(64, 64),
            TieredServiceAreaForecastCodeLengthRule(255),
        ),
    )
}
