package com.acme.transport.terminal.sortation.forecast

import com.acme.transport.kit.text.normalize

/** A rule a sortation forecast must pass. Check returns its problems, or none. */
interface SortationForecastRule {
    fun check(item: SortationForecast): MutableList<String>
}

data class SortationForecastCostRangeRule(var min: Double, var max: Double) : SortationForecastRule {
    override fun check(item: SortationForecast): MutableList<String> {
        val value = item.cost
        if (value < min || value > max) {
            return mutableListOf<String>("cost $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class SortationForecastStartsAtRangeRule(var min: Int, var max: Int) : SortationForecastRule {
    override fun check(item: SortationForecast): MutableList<String> {
        val value = item.startsAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("starts at $value is below $min")
        }
        if (value > max) {
            problems.add("starts at $value is above $max")
        }
        return problems
    }
}

data class SortationForecastAllowedLabelRule(var allowed: MutableList<String>) : SortationForecastRule {
    override fun check(item: SortationForecast): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.label))) {
            return mutableListOf<String>("label ${item.label} is inconsistent")
        }
        return mutableListOf<String>()
    }
}

data class SortationForecastCodePrefixRule(var prefix: String) : SortationForecastRule {
    override fun check(item: SortationForecast): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class SortationForecastCodeLengthRule(var maxLength: Int) : SortationForecastRule {
    override fun check(item: SortationForecast): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class SortationForecastRequiredCodeRule(var message: String) : SortationForecastRule {
    override fun check(item: SortationForecast): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a sortation forecast. */
data class SortationForecastValidator(var rules: MutableList<SortationForecastRule>) {
    fun validate(item: SortationForecast): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: SortationForecast) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<SortationForecast>): MutableMap<String, MutableList<String>> {
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

fun defaultSortationForecastValidator(): SortationForecastValidator {
    return SortationForecastValidator(
        mutableListOf<SortationForecastRule>(
            SortationForecastCostRangeRule(6.9, 1204.3),
            SortationForecastStartsAtRangeRule(3600, 86400),
            SortationForecastAllowedLabelRule(mutableListOf<String>("tiered")),
            SortationForecastCodePrefixRule("OPS"),
            SortationForecastCodeLengthRule(80),
            SortationForecastRequiredCodeRule("sortation forecast needs this field"),
        ),
    )
}
