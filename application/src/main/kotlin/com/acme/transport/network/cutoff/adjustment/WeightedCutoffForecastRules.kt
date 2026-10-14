package com.acme.transport.network.cutoff.adjustment

import com.acme.transport.kit.text.normalize

/** A rule a weighted cutoff forecast must pass. Check returns its problems, or none. */
interface WeightedCutoffForecastRule {
    fun check(item: WeightedCutoffForecast): MutableList<String>
}

data class WeightedCutoffForecastNameLengthRule(var maxLength: Int) : WeightedCutoffForecastRule {
    override fun check(item: WeightedCutoffForecast): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class WeightedCutoffForecastAllowedCategoryRule(
    var allowed: MutableList<String>,
) : WeightedCutoffForecastRule {
    override fun check(item: WeightedCutoffForecast): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.category))) {
            return mutableListOf<String>("category ${item.category} is blocked")
        }
        return mutableListOf<String>()
    }
}

data class WeightedCutoffForecastRequiredContactEmailRule(var message: String) : WeightedCutoffForecastRule {
    override fun check(item: WeightedCutoffForecast): MutableList<String> {
        if (item.contactEmail.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class WeightedCutoffForecastLoadKgRangeRule(
    var min: Double,
    var max: Double,
) : WeightedCutoffForecastRule {
    override fun check(item: WeightedCutoffForecast): MutableList<String> {
        val value = item.loadKg
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("load kg $value is below $min")
        }
        if (value > max) {
            problems.add("load kg $value is above $max")
        }
        return problems
    }
}

data class WeightedCutoffForecastUpdatedAtRangeRule(var min: Int, var max: Int) : WeightedCutoffForecastRule {
    override fun check(item: WeightedCutoffForecast): MutableList<String> {
        val value = item.updatedAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("updated at $value is below $min")
        }
        if (value > max) {
            problems.add("updated at $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a weighted cutoff forecast. */
data class WeightedCutoffForecastValidator(var rules: MutableList<WeightedCutoffForecastRule>) {
    fun validate(item: WeightedCutoffForecast): MutableList<String> {
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

    fun requireValid(item: WeightedCutoffForecast) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<WeightedCutoffForecast>): MutableMap<String, MutableList<String>> {
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

fun defaultWeightedCutoffForecastValidator(): WeightedCutoffForecastValidator {
    return WeightedCutoffForecastValidator(
        mutableListOf<WeightedCutoffForecastRule>(
            WeightedCutoffForecastNameLengthRule(64),
            WeightedCutoffForecastAllowedCategoryRule(mutableListOf<String>()),
            WeightedCutoffForecastRequiredContactEmailRule("weighted cutoff forecast needs a value"),
            WeightedCutoffForecastLoadKgRangeRule(2.7, 1300.9),
            WeightedCutoffForecastUpdatedAtRangeRule(120, 3600),
        ),
    )
}
