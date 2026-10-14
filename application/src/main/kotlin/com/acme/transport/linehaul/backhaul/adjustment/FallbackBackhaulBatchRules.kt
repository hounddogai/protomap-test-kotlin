package com.acme.transport.linehaul.backhaul.adjustment

/** A rule a fallback backhaul batch must pass. Check returns its problems, or none. */
interface FallbackBackhaulBatchRule {
    fun check(item: FallbackBackhaulBatch): MutableList<String>
}

data class FallbackBackhaulBatchRequiredCurrencyRule(var message: String) : FallbackBackhaulBatchRule {
    override fun check(item: FallbackBackhaulBatch): MutableList<String> {
        if (item.currency.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FallbackBackhaulBatchTitleLengthRule(var maxLength: Int) : FallbackBackhaulBatchRule {
    override fun check(item: FallbackBackhaulBatch): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FallbackBackhaulBatchRequiredExternalRefRule(var message: String) : FallbackBackhaulBatchRule {
    override fun check(item: FallbackBackhaulBatch): MutableList<String> {
        if (item.externalRef.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FallbackBackhaulBatchFuelLitersRangeRule(
    var min: Double,
    var max: Double,
) : FallbackBackhaulBatchRule {
    override fun check(item: FallbackBackhaulBatch): MutableList<String> {
        val value = item.fuelLiters
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("fuel liters $value is below $min")
        }
        if (value > max) {
            problems.add("fuel liters $value is above $max")
        }
        return problems
    }
}

data class FallbackBackhaulBatchScoreRangeRule(var min: Double, var max: Double) : FallbackBackhaulBatchRule {
    override fun check(item: FallbackBackhaulBatch): MutableList<String> {
        val value = item.score
        if (value < min || value > max) {
            return mutableListOf<String>("score $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class FallbackBackhaulBatchRequiredCodeRule(var message: String) : FallbackBackhaulBatchRule {
    override fun check(item: FallbackBackhaulBatch): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a fallback backhaul batch. */
data class FallbackBackhaulBatchValidator(var rules: MutableList<FallbackBackhaulBatchRule>) {
    fun validate(item: FallbackBackhaulBatch): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: FallbackBackhaulBatch) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FallbackBackhaulBatch>): MutableMap<String, MutableList<String>> {
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

fun defaultFallbackBackhaulBatchValidator(): FallbackBackhaulBatchValidator {
    return FallbackBackhaulBatchValidator(
        mutableListOf<FallbackBackhaulBatchRule>(
            FallbackBackhaulBatchRequiredCurrencyRule("fallback backhaul batch needs a value"),
            FallbackBackhaulBatchTitleLengthRule(64),
            FallbackBackhaulBatchRequiredExternalRefRule("fallback backhaul batch needs a value"),
            FallbackBackhaulBatchFuelLitersRangeRule(8.0, 1326.3),
            FallbackBackhaulBatchScoreRangeRule(9.8, 3180.3),
            FallbackBackhaulBatchRequiredCodeRule("fallback backhaul batch needs this field"),
        ),
    )
}
