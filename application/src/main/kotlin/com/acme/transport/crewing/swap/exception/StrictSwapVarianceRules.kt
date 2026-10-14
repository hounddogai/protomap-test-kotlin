package com.acme.transport.crewing.swap.exception

/** A rule a strict swap variance must pass. Check returns its problems, or none. */
interface StrictSwapVarianceRule {
    fun check(item: StrictSwapVariance): MutableList<String>
}

data class StrictSwapVarianceUnitLengthRule(var maxLength: Int) : StrictSwapVarianceRule {
    override fun check(item: StrictSwapVariance): MutableList<String> {
        val size = item.unit.length
        if (size > maxLength) {
            return mutableListOf<String>("unit is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class StrictSwapVarianceRequiredUnitRule(var message: String) : StrictSwapVarianceRule {
    override fun check(item: StrictSwapVariance): MutableList<String> {
        if (item.unit.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class StrictSwapVarianceMaximumRangeRule(var min: Double, var max: Double) : StrictSwapVarianceRule {
    override fun check(item: StrictSwapVariance): MutableList<String> {
        val value = item.maximum
        if (value < min || value > max) {
            return mutableListOf<String>("maximum $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class StrictSwapVarianceCodePrefixRule(var prefix: String) : StrictSwapVarianceRule {
    override fun check(item: StrictSwapVariance): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class StrictSwapVarianceTitleLengthRule(var maxLength: Int) : StrictSwapVarianceRule {
    override fun check(item: StrictSwapVariance): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class StrictSwapVarianceRequiredCodeRule(var message: String) : StrictSwapVarianceRule {
    override fun check(item: StrictSwapVariance): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a strict swap variance. */
data class StrictSwapVarianceValidator(var rules: MutableList<StrictSwapVarianceRule>) {
    fun validate(item: StrictSwapVariance): MutableList<String> {
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

    fun requireValid(item: StrictSwapVariance) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<StrictSwapVariance>): MutableMap<String, MutableList<String>> {
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

fun defaultStrictSwapVarianceValidator(): StrictSwapVarianceValidator {
    return StrictSwapVarianceValidator(
        mutableListOf<StrictSwapVarianceRule>(
            StrictSwapVarianceUnitLengthRule(32),
            StrictSwapVarianceRequiredUnitRule("strict swap variance needs a value"),
            StrictSwapVarianceMaximumRangeRule(0.0, 4462.8),
            StrictSwapVarianceCodePrefixRule("OPS"),
            StrictSwapVarianceTitleLengthRule(3600),
            StrictSwapVarianceRequiredCodeRule("strict swap variance needs a value"),
        ),
    )
}
