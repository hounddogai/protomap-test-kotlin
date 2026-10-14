package com.acme.transport.emissions.fuelburn.claim

import com.acme.transport.kit.text.normalize

/** A rule a batched fuel burn forecast must pass. Check returns its problems, or none. */
interface BatchedFuelBurnForecastRule {
    fun check(item: BatchedFuelBurnForecast): MutableList<String>
}

data class BatchedFuelBurnForecastRequiredCodeRule(var message: String) : BatchedFuelBurnForecastRule {
    override fun check(item: BatchedFuelBurnForecast): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class BatchedFuelBurnForecastAllowedSourceRule(
    var allowed: MutableList<String>,
) : BatchedFuelBurnForecastRule {
    override fun check(item: BatchedFuelBurnForecast): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.source))) {
            return mutableListOf<String>("source ${item.source} is blocked")
        }
        return mutableListOf<String>()
    }
}

data class BatchedFuelBurnForecastWindowDaysRangeRule(
    var min: Int,
    var max: Int,
) : BatchedFuelBurnForecastRule {
    override fun check(item: BatchedFuelBurnForecast): MutableList<String> {
        val value = item.windowDays
        if (value < min || value > max) {
            return mutableListOf<String>("window days $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class BatchedFuelBurnForecastRequiredSourceRule(var message: String) : BatchedFuelBurnForecastRule {
    override fun check(item: BatchedFuelBurnForecast): MutableList<String> {
        if (item.source.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class BatchedFuelBurnForecastRequiredDockRule(var message: String) : BatchedFuelBurnForecastRule {
    override fun check(item: BatchedFuelBurnForecast): MutableList<String> {
        if (item.dock.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a batched fuel burn forecast. */
data class BatchedFuelBurnForecastValidator(var rules: MutableList<BatchedFuelBurnForecastRule>) {
    fun validate(item: BatchedFuelBurnForecast): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: BatchedFuelBurnForecast) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<BatchedFuelBurnForecast>): MutableMap<String, MutableList<String>> {
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

fun defaultBatchedFuelBurnForecastValidator(): BatchedFuelBurnForecastValidator {
    return BatchedFuelBurnForecastValidator(
        mutableListOf<BatchedFuelBurnForecastRule>(
            BatchedFuelBurnForecastRequiredCodeRule("batched fuel burn forecast needs an entry"),
            BatchedFuelBurnForecastAllowedSourceRule(mutableListOf<String>("primary", "fallback")),
            BatchedFuelBurnForecastWindowDaysRangeRule(32, 255),
            BatchedFuelBurnForecastRequiredSourceRule("batched fuel burn forecast needs this field"),
            BatchedFuelBurnForecastRequiredDockRule("batched fuel burn forecast needs this field"),
        ),
    )
}
