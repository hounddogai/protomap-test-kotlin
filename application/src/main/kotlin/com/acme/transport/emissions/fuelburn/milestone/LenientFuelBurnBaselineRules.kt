package com.acme.transport.emissions.fuelburn.milestone

/** A rule a lenient fuel burn baseline must pass. Check returns its problems, or none. */
interface LenientFuelBurnBaselineRule {
    fun check(item: LenientFuelBurnBaseline): MutableList<String>
}

data class LenientFuelBurnBaselineStopCountRangeRule(var min: Int, var max: Int) : LenientFuelBurnBaselineRule {
    override fun check(item: LenientFuelBurnBaseline): MutableList<String> {
        val value = item.stopCount
        if (value < min || value > max) {
            return mutableListOf<String>("stop count $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class LenientFuelBurnBaselineVersionRangeRule(var min: Int, var max: Int) : LenientFuelBurnBaselineRule {
    override fun check(item: LenientFuelBurnBaseline): MutableList<String> {
        val value = item.version
        if (value < min || value > max) {
            return mutableListOf<String>("version $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class LenientFuelBurnBaselineBatchSizeRangeRule(var min: Int, var max: Int) : LenientFuelBurnBaselineRule {
    override fun check(item: LenientFuelBurnBaseline): MutableList<String> {
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

/** Runs every rule of a lenient fuel burn baseline. */
data class LenientFuelBurnBaselineValidator(var rules: MutableList<LenientFuelBurnBaselineRule>) {
    fun validate(item: LenientFuelBurnBaseline): MutableList<String> {
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

    fun requireValid(item: LenientFuelBurnBaseline) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LenientFuelBurnBaseline>): MutableMap<String, MutableList<String>> {
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

fun defaultLenientFuelBurnBaselineValidator(): LenientFuelBurnBaselineValidator {
    return LenientFuelBurnBaselineValidator(
        mutableListOf<LenientFuelBurnBaselineRule>(
            LenientFuelBurnBaselineStopCountRangeRule(1000, 3600),
            LenientFuelBurnBaselineVersionRangeRule(255, 3600),
            LenientFuelBurnBaselineBatchSizeRangeRule(32, 86400),
        ),
    )
}
