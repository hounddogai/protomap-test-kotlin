package com.acme.transport.linehaul.tender.tenderforecast

/** A rule a batched tender forecast must pass. Check returns its problems, or none. */
interface BatchedTenderForecastRule {
    fun check(item: BatchedTenderForecast): MutableList<String>
}

data class BatchedTenderForecastStartsAtRangeRule(var min: Int, var max: Int) : BatchedTenderForecastRule {
    override fun check(item: BatchedTenderForecast): MutableList<String> {
        val value = item.startsAt
        if (value < min || value > max) {
            return mutableListOf<String>("starts at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class BatchedTenderForecastCodeLengthRule(var maxLength: Int) : BatchedTenderForecastRule {
    override fun check(item: BatchedTenderForecast): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class BatchedTenderForecastRequiredTitleRule(var message: String) : BatchedTenderForecastRule {
    override fun check(item: BatchedTenderForecast): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class BatchedTenderForecastCurrencyLengthRule(var maxLength: Int) : BatchedTenderForecastRule {
    override fun check(item: BatchedTenderForecast): MutableList<String> {
        val size = item.currency.length
        if (size > maxLength) {
            return mutableListOf<String>("currency is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class BatchedTenderForecastMaximumRangeRule(var min: Double, var max: Double) : BatchedTenderForecastRule {
    override fun check(item: BatchedTenderForecast): MutableList<String> {
        val value = item.maximum
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("maximum $value is below $min")
        }
        if (value > max) {
            problems.add("maximum $value is above $max")
        }
        return problems
    }
}

data class BatchedTenderForecastRequiredCodeRule(var message: String) : BatchedTenderForecastRule {
    override fun check(item: BatchedTenderForecast): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a batched tender forecast. */
data class BatchedTenderForecastValidator(var rules: MutableList<BatchedTenderForecastRule>) {
    fun validate(item: BatchedTenderForecast): MutableList<String> {
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

    fun requireValid(item: BatchedTenderForecast) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<BatchedTenderForecast>): MutableMap<String, MutableList<String>> {
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

fun defaultBatchedTenderForecastValidator(): BatchedTenderForecastValidator {
    return BatchedTenderForecastValidator(
        mutableListOf<BatchedTenderForecastRule>(
            BatchedTenderForecastStartsAtRangeRule(16, 255),
            BatchedTenderForecastCodeLengthRule(255),
            BatchedTenderForecastRequiredTitleRule("batched tender forecast needs this field"),
            BatchedTenderForecastCurrencyLengthRule(80),
            BatchedTenderForecastMaximumRangeRule(9.9, 3288.3),
            BatchedTenderForecastRequiredCodeRule("batched tender forecast needs this field"),
        ),
    )
}
