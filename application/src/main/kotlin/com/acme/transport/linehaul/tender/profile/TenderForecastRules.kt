package com.acme.transport.linehaul.tender.profile

/** A rule a tender forecast must pass. Check returns its problems, or none. */
interface TenderForecastRule {
    fun check(item: TenderForecast): MutableList<String>
}

data class TenderForecastCodePrefixRule(var prefix: String) : TenderForecastRule {
    override fun check(item: TenderForecast): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class TenderForecastMarginRangeRule(var min: Double, var max: Double) : TenderForecastRule {
    override fun check(item: TenderForecast): MutableList<String> {
        val value = item.margin
        if (value < min || value > max) {
            return mutableListOf<String>("margin $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class TenderForecastRequiredCodeRule(var message: String) : TenderForecastRule {
    override fun check(item: TenderForecast): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TenderForecastCodeLengthRule(var maxLength: Int) : TenderForecastRule {
    override fun check(item: TenderForecast): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TenderForecastLoadKgRangeRule(var min: Double, var max: Double) : TenderForecastRule {
    override fun check(item: TenderForecast): MutableList<String> {
        val value = item.loadKg
        if (value < min || value > max) {
            return mutableListOf<String>("load kg $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class TenderForecastBatchSizeRangeRule(var min: Int, var max: Int) : TenderForecastRule {
    override fun check(item: TenderForecast): MutableList<String> {
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

/** Runs every rule of a tender forecast. */
data class TenderForecastValidator(var rules: MutableList<TenderForecastRule>) {
    fun validate(item: TenderForecast): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: TenderForecast) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TenderForecast>): MutableMap<String, MutableList<String>> {
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

fun defaultTenderForecastValidator(): TenderForecastValidator {
    return TenderForecastValidator(
        mutableListOf<TenderForecastRule>(
            TenderForecastCodePrefixRule("Q"),
            TenderForecastMarginRangeRule(9.3, 2251.5),
            TenderForecastRequiredCodeRule("tender forecast needs this field"),
            TenderForecastCodeLengthRule(120),
            TenderForecastLoadKgRangeRule(4.9, 428.3),
            TenderForecastBatchSizeRangeRule(16, 64),
        ),
    )
}
