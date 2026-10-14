package com.acme.transport.terminal.sortation.policy

/** A rule a batched sortation forecast must pass. Check returns its problems, or none. */
interface BatchedSortationForecastRule {
    fun check(item: BatchedSortationForecast): MutableList<String>
}

data class BatchedSortationForecastCarrierLengthRule(var maxLength: Int) : BatchedSortationForecastRule {
    override fun check(item: BatchedSortationForecast): MutableList<String> {
        val size = item.carrier.length
        if (size > maxLength) {
            return mutableListOf<String>("carrier is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class BatchedSortationForecastCodeLengthRule(var maxLength: Int) : BatchedSortationForecastRule {
    override fun check(item: BatchedSortationForecast): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class BatchedSortationForecastCeilingRangeRule(
    var min: Double,
    var max: Double,
) : BatchedSortationForecastRule {
    override fun check(item: BatchedSortationForecast): MutableList<String> {
        val value = item.ceiling
        if (value < min || value > max) {
            return mutableListOf<String>("ceiling $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class BatchedSortationForecastDescriptionLengthRule(var maxLength: Int) : BatchedSortationForecastRule {
    override fun check(item: BatchedSortationForecast): MutableList<String> {
        val size = item.description.length
        if (size > maxLength) {
            return mutableListOf<String>("description is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a batched sortation forecast. */
data class BatchedSortationForecastValidator(var rules: MutableList<BatchedSortationForecastRule>) {
    fun validate(item: BatchedSortationForecast): MutableList<String> {
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

    fun requireValid(item: BatchedSortationForecast) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<BatchedSortationForecast>): MutableMap<String, MutableList<String>> {
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

fun defaultBatchedSortationForecastValidator(): BatchedSortationForecastValidator {
    return BatchedSortationForecastValidator(
        mutableListOf<BatchedSortationForecastRule>(
            BatchedSortationForecastCarrierLengthRule(16),
            BatchedSortationForecastCodeLengthRule(255),
            BatchedSortationForecastCeilingRangeRule(8.0, 1353.3),
            BatchedSortationForecastDescriptionLengthRule(80),
        ),
    )
}
