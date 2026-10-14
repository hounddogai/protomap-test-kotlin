package com.acme.transport.fleetops.utilization.window

/** A rule a utilization rollup must pass. Check returns its problems, or none. */
interface UtilizationRollupRule {
    fun check(item: UtilizationRollup): MutableList<String>
}

data class UtilizationRollupDescriptionLengthRule(var maxLength: Int) : UtilizationRollupRule {
    override fun check(item: UtilizationRollup): MutableList<String> {
        val size = item.description.length
        if (size > maxLength) {
            return mutableListOf<String>("description is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class UtilizationRollupNameLengthRule(var maxLength: Int) : UtilizationRollupRule {
    override fun check(item: UtilizationRollup): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class UtilizationRollupMinimumRangeRule(var min: Double, var max: Double) : UtilizationRollupRule {
    override fun check(item: UtilizationRollup): MutableList<String> {
        val value = item.minimum
        if (value < min || value > max) {
            return mutableListOf<String>("minimum $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class UtilizationRollupCategoryLengthRule(var maxLength: Int) : UtilizationRollupRule {
    override fun check(item: UtilizationRollup): MutableList<String> {
        val size = item.category.length
        if (size > maxLength) {
            return mutableListOf<String>("category is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a utilization rollup. */
data class UtilizationRollupValidator(var rules: MutableList<UtilizationRollupRule>) {
    fun validate(item: UtilizationRollup): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: UtilizationRollup) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<UtilizationRollup>): MutableMap<String, MutableList<String>> {
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

fun defaultUtilizationRollupValidator(): UtilizationRollupValidator {
    return UtilizationRollupValidator(
        mutableListOf<UtilizationRollupRule>(
            UtilizationRollupDescriptionLengthRule(86400),
            UtilizationRollupNameLengthRule(120),
            UtilizationRollupMinimumRangeRule(9.4, 1107.6),
            UtilizationRollupCategoryLengthRule(80),
        ),
    )
}
