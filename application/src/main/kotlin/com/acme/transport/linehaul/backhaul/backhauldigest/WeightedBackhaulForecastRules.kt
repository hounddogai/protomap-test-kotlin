package com.acme.transport.linehaul.backhaul.backhauldigest

import com.acme.transport.kit.text.normalize

/** A rule a weighted backhaul forecast must pass. Check returns its problems, or none. */
interface WeightedBackhaulForecastRule {
    fun check(item: WeightedBackhaulForecast): MutableList<String>
}

data class WeightedBackhaulForecastRequiredCodeRule(var message: String) : WeightedBackhaulForecastRule {
    override fun check(item: WeightedBackhaulForecast): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class WeightedBackhaulForecastRequiredTitleRule(var message: String) : WeightedBackhaulForecastRule {
    override fun check(item: WeightedBackhaulForecast): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class WeightedBackhaulForecastReviewerLengthRule(var maxLength: Int) : WeightedBackhaulForecastRule {
    override fun check(item: WeightedBackhaulForecast): MutableList<String> {
        val size = item.reviewer.length
        if (size > maxLength) {
            return mutableListOf<String>("reviewer is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class WeightedBackhaulForecastAllowedTitleRule(
    var allowed: MutableList<String>,
) : WeightedBackhaulForecastRule {
    override fun check(item: WeightedBackhaulForecast): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.title))) {
            return mutableListOf<String>("title ${item.title} is above maximum")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a weighted backhaul forecast. */
data class WeightedBackhaulForecastValidator(var rules: MutableList<WeightedBackhaulForecastRule>) {
    fun validate(item: WeightedBackhaulForecast): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: WeightedBackhaulForecast) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<WeightedBackhaulForecast>): MutableMap<String, MutableList<String>> {
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

fun defaultWeightedBackhaulForecastValidator(): WeightedBackhaulForecastValidator {
    return WeightedBackhaulForecastValidator(
        mutableListOf<WeightedBackhaulForecastRule>(
            WeightedBackhaulForecastRequiredCodeRule("weighted backhaul forecast needs an entry"),
            WeightedBackhaulForecastRequiredTitleRule("weighted backhaul forecast needs this field"),
            WeightedBackhaulForecastReviewerLengthRule(3600),
            WeightedBackhaulForecastAllowedTitleRule(mutableListOf<String>("seasonal", "rolling", "primary")),
        ),
    )
}
