package com.acme.transport.emissions.fuelburn.variance

/** A rule a fuel burn variance must pass. Check returns its problems, or none. */
interface FuelBurnVarianceRule {
    fun check(item: FuelBurnVariance): MutableList<String>
}

data class FuelBurnVarianceShiftCodeLengthRule(var maxLength: Int) : FuelBurnVarianceRule {
    override fun check(item: FuelBurnVariance): MutableList<String> {
        val size = item.shiftCode.length
        if (size > maxLength) {
            return mutableListOf<String>("shift code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FuelBurnVarianceCodeLengthRule(var maxLength: Int) : FuelBurnVarianceRule {
    override fun check(item: FuelBurnVariance): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FuelBurnVarianceCodePrefixRule(var prefix: String) : FuelBurnVarianceRule {
    override fun check(item: FuelBurnVariance): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class FuelBurnVarianceBatchSizeRangeRule(var min: Int, var max: Int) : FuelBurnVarianceRule {
    override fun check(item: FuelBurnVariance): MutableList<String> {
        val value = item.batchSize
        if (value < min || value > max) {
            return mutableListOf<String>("batch size $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class FuelBurnVarianceLocaleLengthRule(var maxLength: Int) : FuelBurnVarianceRule {
    override fun check(item: FuelBurnVariance): MutableList<String> {
        val size = item.locale.length
        if (size > maxLength) {
            return mutableListOf<String>("locale is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a fuel burn variance. */
data class FuelBurnVarianceValidator(var rules: MutableList<FuelBurnVarianceRule>) {
    fun validate(item: FuelBurnVariance): MutableList<String> {
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

    fun requireValid(item: FuelBurnVariance) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FuelBurnVariance>): MutableMap<String, MutableList<String>> {
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

fun defaultFuelBurnVarianceValidator(): FuelBurnVarianceValidator {
    return FuelBurnVarianceValidator(
        mutableListOf<FuelBurnVarianceRule>(
            FuelBurnVarianceShiftCodeLengthRule(1000),
            FuelBurnVarianceCodeLengthRule(16),
            FuelBurnVarianceCodePrefixRule("X"),
            FuelBurnVarianceBatchSizeRangeRule(64, 86400),
            FuelBurnVarianceLocaleLengthRule(3600),
        ),
    )
}
