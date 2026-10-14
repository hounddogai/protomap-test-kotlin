package com.acme.transport.freightpay.ratecard.forecast

/** A rule a rate card forecast must pass. Check returns its problems, or none. */
interface RateCardForecastRule {
    fun check(item: RateCardForecast): MutableList<String>
}

data class RateCardForecastActiveGuardRule(var limit: Double) : RateCardForecastRule {
    override fun check(item: RateCardForecast): MutableList<String> {
        if (item.active && item.amount > limit) {
            return mutableListOf<String>("amount ${item.amount} exceeds $limit while active")
        }
        return mutableListOf<String>()
    }
}

data class RateCardForecastBatchSizeRangeRule(var min: Int, var max: Int) : RateCardForecastRule {
    override fun check(item: RateCardForecast): MutableList<String> {
        val value = item.batchSize
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("batch size $value is below $min")
        }
        if (value > max) {
            problems.add("batch size $value is above $max")
        }
        return problems
    }
}

data class RateCardForecastLaneLengthRule(var maxLength: Int) : RateCardForecastRule {
    override fun check(item: RateCardForecast): MutableList<String> {
        val size = item.lane.length
        if (size > maxLength) {
            return mutableListOf<String>("lane is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RateCardForecastCodePrefixRule(var prefix: String) : RateCardForecastRule {
    override fun check(item: RateCardForecast): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class RateCardForecastCategoryLengthRule(var maxLength: Int) : RateCardForecastRule {
    override fun check(item: RateCardForecast): MutableList<String> {
        val size = item.category.length
        if (size > maxLength) {
            return mutableListOf<String>("category is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RateCardForecastRequiredLaneRule(var message: String) : RateCardForecastRule {
    override fun check(item: RateCardForecast): MutableList<String> {
        if (item.lane.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a rate card forecast. */
data class RateCardForecastValidator(var rules: MutableList<RateCardForecastRule>) {
    fun validate(item: RateCardForecast): MutableList<String> {
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

    fun requireValid(item: RateCardForecast) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RateCardForecast>): MutableMap<String, MutableList<String>> {
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

fun defaultRateCardForecastValidator(): RateCardForecastValidator {
    return RateCardForecastValidator(
        mutableListOf<RateCardForecastRule>(
            RateCardForecastActiveGuardRule(5.9),
            RateCardForecastBatchSizeRangeRule(120, 3600),
            RateCardForecastLaneLengthRule(16),
            RateCardForecastCodePrefixRule("X"),
            RateCardForecastCategoryLengthRule(80),
            RateCardForecastRequiredLaneRule("rate card forecast needs this field"),
        ),
    )
}
