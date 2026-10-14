package com.acme.transport.crewing.restbreak.breakvariance

/** A rule a weighted rest break rollup must pass. Check returns its problems, or none. */
interface WeightedRestBreakRollupRule {
    fun check(item: WeightedRestBreakRollup): MutableList<String>
}

data class WeightedRestBreakRollupCreatedAtRangeRule(var min: Int, var max: Int) : WeightedRestBreakRollupRule {
    override fun check(item: WeightedRestBreakRollup): MutableList<String> {
        val value = item.createdAt
        if (value < min || value > max) {
            return mutableListOf<String>("created at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class WeightedRestBreakRollupLabelLengthRule(var maxLength: Int) : WeightedRestBreakRollupRule {
    override fun check(item: WeightedRestBreakRollup): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class WeightedRestBreakRollupCategoryLengthRule(var maxLength: Int) : WeightedRestBreakRollupRule {
    override fun check(item: WeightedRestBreakRollup): MutableList<String> {
        val size = item.category.length
        if (size > maxLength) {
            return mutableListOf<String>("category is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class WeightedRestBreakRollupDiscountRangeRule(
    var min: Double,
    var max: Double,
) : WeightedRestBreakRollupRule {
    override fun check(item: WeightedRestBreakRollup): MutableList<String> {
        val value = item.discount
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("discount $value is below $min")
        }
        if (value > max) {
            problems.add("discount $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a weighted rest break rollup. */
data class WeightedRestBreakRollupValidator(var rules: MutableList<WeightedRestBreakRollupRule>) {
    fun validate(item: WeightedRestBreakRollup): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: WeightedRestBreakRollup) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<WeightedRestBreakRollup>): MutableMap<String, MutableList<String>> {
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

fun defaultWeightedRestBreakRollupValidator(): WeightedRestBreakRollupValidator {
    return WeightedRestBreakRollupValidator(
        mutableListOf<WeightedRestBreakRollupRule>(
            WeightedRestBreakRollupCreatedAtRangeRule(64, 120),
            WeightedRestBreakRollupLabelLengthRule(32),
            WeightedRestBreakRollupCategoryLengthRule(255),
            WeightedRestBreakRollupDiscountRangeRule(6.8, 659.2),
        ),
    )
}
