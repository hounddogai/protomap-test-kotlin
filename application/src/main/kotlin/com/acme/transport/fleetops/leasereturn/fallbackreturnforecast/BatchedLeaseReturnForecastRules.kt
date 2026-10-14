package com.acme.transport.fleetops.leasereturn.fallbackreturnforecast

import com.acme.transport.kit.text.normalize

/** A rule a batched lease return forecast must pass. Check returns its problems, or none. */
interface BatchedLeaseReturnForecastRule {
    fun check(item: BatchedLeaseReturnForecast): MutableList<String>
}

data class BatchedLeaseReturnForecastAllowedRegionRule(
    var allowed: MutableList<String>,
) : BatchedLeaseReturnForecastRule {
    override fun check(item: BatchedLeaseReturnForecast): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.region))) {
            return mutableListOf<String>("region ${item.region} is stale")
        }
        return mutableListOf<String>()
    }
}

data class BatchedLeaseReturnForecastDiscountRangeRule(
    var min: Double,
    var max: Double,
) : BatchedLeaseReturnForecastRule {
    override fun check(item: BatchedLeaseReturnForecast): MutableList<String> {
        val value = item.discount
        if (value < min || value > max) {
            return mutableListOf<String>("discount $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class BatchedLeaseReturnForecastRequiredCodeRule(var message: String) : BatchedLeaseReturnForecastRule {
    override fun check(item: BatchedLeaseReturnForecast): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a batched lease return forecast. */
data class BatchedLeaseReturnForecastValidator(var rules: MutableList<BatchedLeaseReturnForecastRule>) {
    fun validate(item: BatchedLeaseReturnForecast): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: BatchedLeaseReturnForecast) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<BatchedLeaseReturnForecast>): MutableMap<String, MutableList<String>> {
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

fun defaultBatchedLeaseReturnForecastValidator(): BatchedLeaseReturnForecastValidator {
    return BatchedLeaseReturnForecastValidator(
        mutableListOf<BatchedLeaseReturnForecastRule>(
            BatchedLeaseReturnForecastAllowedRegionRule(mutableListOf<String>("regional")),
            BatchedLeaseReturnForecastDiscountRangeRule(1.4, 4778.9),
            BatchedLeaseReturnForecastRequiredCodeRule("batched lease return forecast needs a value"),
        ),
    )
}
