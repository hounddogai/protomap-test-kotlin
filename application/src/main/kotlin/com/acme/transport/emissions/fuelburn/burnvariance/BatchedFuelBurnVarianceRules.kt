package com.acme.transport.emissions.fuelburn.burnvariance

/** A rule a batched fuel burn variance must pass. Check returns its problems, or none. */
interface BatchedFuelBurnVarianceRule {
    fun check(item: BatchedFuelBurnVariance): MutableList<String>
}

data class BatchedFuelBurnVarianceShiftCodeLengthRule(var maxLength: Int) : BatchedFuelBurnVarianceRule {
    override fun check(item: BatchedFuelBurnVariance): MutableList<String> {
        val size = item.shiftCode.length
        if (size > maxLength) {
            return mutableListOf<String>("shift code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class BatchedFuelBurnVarianceRequiredLabelRule(var message: String) : BatchedFuelBurnVarianceRule {
    override fun check(item: BatchedFuelBurnVariance): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class BatchedFuelBurnVarianceStartsAtRangeRule(var min: Int, var max: Int) : BatchedFuelBurnVarianceRule {
    override fun check(item: BatchedFuelBurnVariance): MutableList<String> {
        val value = item.startsAt
        if (value < min || value > max) {
            return mutableListOf<String>("starts at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class BatchedFuelBurnVarianceMinimumRangeRule(
    var min: Double,
    var max: Double,
) : BatchedFuelBurnVarianceRule {
    override fun check(item: BatchedFuelBurnVariance): MutableList<String> {
        val value = item.minimum
        if (value < min || value > max) {
            return mutableListOf<String>("minimum $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class BatchedFuelBurnVarianceCodePrefixRule(var prefix: String) : BatchedFuelBurnVarianceRule {
    override fun check(item: BatchedFuelBurnVariance): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a batched fuel burn variance. */
data class BatchedFuelBurnVarianceValidator(var rules: MutableList<BatchedFuelBurnVarianceRule>) {
    fun validate(item: BatchedFuelBurnVariance): MutableList<String> {
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

    fun requireValid(item: BatchedFuelBurnVariance) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<BatchedFuelBurnVariance>): MutableMap<String, MutableList<String>> {
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

fun defaultBatchedFuelBurnVarianceValidator(): BatchedFuelBurnVarianceValidator {
    return BatchedFuelBurnVarianceValidator(
        mutableListOf<BatchedFuelBurnVarianceRule>(
            BatchedFuelBurnVarianceShiftCodeLengthRule(64),
            BatchedFuelBurnVarianceRequiredLabelRule("batched fuel burn variance needs this field"),
            BatchedFuelBurnVarianceStartsAtRangeRule(255, 86400),
            BatchedFuelBurnVarianceMinimumRangeRule(8.3, 362.3),
            BatchedFuelBurnVarianceCodePrefixRule("Q"),
        ),
    )
}
