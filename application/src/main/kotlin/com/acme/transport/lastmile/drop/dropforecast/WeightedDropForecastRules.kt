package com.acme.transport.lastmile.drop.dropforecast

/** A rule a weighted drop forecast must pass. Check returns its problems, or none. */
interface WeightedDropForecastRule {
    fun check(item: WeightedDropForecast): MutableList<String>
}

data class WeightedDropForecastCodePrefixRule(var prefix: String) : WeightedDropForecastRule {
    override fun check(item: WeightedDropForecast): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class WeightedDropForecastTitleLengthRule(var maxLength: Int) : WeightedDropForecastRule {
    override fun check(item: WeightedDropForecast): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class WeightedDropForecastLabelLengthRule(var maxLength: Int) : WeightedDropForecastRule {
    override fun check(item: WeightedDropForecast): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class WeightedDropForecastUpdatedAtRangeRule(var min: Int, var max: Int) : WeightedDropForecastRule {
    override fun check(item: WeightedDropForecast): MutableList<String> {
        val value = item.updatedAt
        if (value < min || value > max) {
            return mutableListOf<String>("updated at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a weighted drop forecast. */
data class WeightedDropForecastValidator(var rules: MutableList<WeightedDropForecastRule>) {
    fun validate(item: WeightedDropForecast): MutableList<String> {
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

    fun requireValid(item: WeightedDropForecast) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<WeightedDropForecast>): MutableMap<String, MutableList<String>> {
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

fun defaultWeightedDropForecastValidator(): WeightedDropForecastValidator {
    return WeightedDropForecastValidator(
        mutableListOf<WeightedDropForecastRule>(
            WeightedDropForecastCodePrefixRule("REF"),
            WeightedDropForecastTitleLengthRule(120),
            WeightedDropForecastLabelLengthRule(1000),
            WeightedDropForecastUpdatedAtRangeRule(3600, 3600),
        ),
    )
}
