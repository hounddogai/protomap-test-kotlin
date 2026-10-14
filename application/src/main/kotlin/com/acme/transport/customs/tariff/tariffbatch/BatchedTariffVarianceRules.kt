package com.acme.transport.customs.tariff.tariffbatch

/** A rule a batched tariff variance must pass. Check returns its problems, or none. */
interface BatchedTariffVarianceRule {
    fun check(item: BatchedTariffVariance): MutableList<String>
}

data class BatchedTariffVarianceCodeLengthRule(var maxLength: Int) : BatchedTariffVarianceRule {
    override fun check(item: BatchedTariffVariance): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class BatchedTariffVarianceSourceLengthRule(var maxLength: Int) : BatchedTariffVarianceRule {
    override fun check(item: BatchedTariffVariance): MutableList<String> {
        val size = item.source.length
        if (size > maxLength) {
            return mutableListOf<String>("source is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class BatchedTariffVarianceCodePrefixRule(var prefix: String) : BatchedTariffVarianceRule {
    override fun check(item: BatchedTariffVariance): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class BatchedTariffVarianceBatchSizeRangeRule(var min: Int, var max: Int) : BatchedTariffVarianceRule {
    override fun check(item: BatchedTariffVariance): MutableList<String> {
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

data class BatchedTariffVarianceFuelLitersRangeRule(
    var min: Double,
    var max: Double,
) : BatchedTariffVarianceRule {
    override fun check(item: BatchedTariffVariance): MutableList<String> {
        val value = item.fuelLiters
        if (value < min || value > max) {
            return mutableListOf<String>("fuel liters $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a batched tariff variance. */
data class BatchedTariffVarianceValidator(var rules: MutableList<BatchedTariffVarianceRule>) {
    fun validate(item: BatchedTariffVariance): MutableList<String> {
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

    fun requireValid(item: BatchedTariffVariance) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<BatchedTariffVariance>): MutableMap<String, MutableList<String>> {
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

fun defaultBatchedTariffVarianceValidator(): BatchedTariffVarianceValidator {
    return BatchedTariffVarianceValidator(
        mutableListOf<BatchedTariffVarianceRule>(
            BatchedTariffVarianceCodeLengthRule(255),
            BatchedTariffVarianceSourceLengthRule(255),
            BatchedTariffVarianceCodePrefixRule("ACM"),
            BatchedTariffVarianceBatchSizeRangeRule(16, 120),
            BatchedTariffVarianceFuelLitersRangeRule(4.7, 1299.4),
        ),
    )
}
