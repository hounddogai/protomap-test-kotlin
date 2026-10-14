package com.acme.transport.safety.cameraevent.eventscore

import com.acme.transport.kit.text.normalize

/** A rule a weighted camera event forecast must pass. Check returns its problems, or none. */
interface WeightedCameraEventForecastRule {
    fun check(item: WeightedCameraEventForecast): MutableList<String>
}

data class WeightedCameraEventForecastRequiredCodeRule(var message: String) : WeightedCameraEventForecastRule {
    override fun check(item: WeightedCameraEventForecast): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class WeightedCameraEventForecastTitleLengthRule(var maxLength: Int) : WeightedCameraEventForecastRule {
    override fun check(item: WeightedCameraEventForecast): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class WeightedCameraEventForecastRequiredTitleRule(var message: String) : WeightedCameraEventForecastRule {
    override fun check(item: WeightedCameraEventForecast): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class WeightedCameraEventForecastAllowedTitleRule(
    var allowed: MutableList<String>,
) : WeightedCameraEventForecastRule {
    override fun check(item: WeightedCameraEventForecast): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.title))) {
            return mutableListOf<String>("title ${item.title} is stale")
        }
        return mutableListOf<String>()
    }
}

data class WeightedCameraEventForecastTimezoneLengthRule(var maxLength: Int) : WeightedCameraEventForecastRule {
    override fun check(item: WeightedCameraEventForecast): MutableList<String> {
        val size = item.timezone.length
        if (size > maxLength) {
            return mutableListOf<String>("timezone is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a weighted camera event forecast. */
data class WeightedCameraEventForecastValidator(var rules: MutableList<WeightedCameraEventForecastRule>) {
    fun validate(item: WeightedCameraEventForecast): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: WeightedCameraEventForecast) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<WeightedCameraEventForecast>): MutableMap<String, MutableList<String>> {
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

fun defaultWeightedCameraEventForecastValidator(): WeightedCameraEventForecastValidator {
    return WeightedCameraEventForecastValidator(
        mutableListOf<WeightedCameraEventForecastRule>(
            WeightedCameraEventForecastRequiredCodeRule("weighted camera event forecast needs a value"),
            WeightedCameraEventForecastTitleLengthRule(255),
            WeightedCameraEventForecastRequiredTitleRule("weighted camera event forecast needs a value"),
            WeightedCameraEventForecastAllowedTitleRule(
                mutableListOf<String>("adaptive", "scheduled", "regional"),
            ),
            WeightedCameraEventForecastTimezoneLengthRule(80),
        ),
    )
}
