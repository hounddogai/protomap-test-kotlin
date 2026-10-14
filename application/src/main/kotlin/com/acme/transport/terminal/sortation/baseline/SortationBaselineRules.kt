package com.acme.transport.terminal.sortation.baseline

/** A rule a sortation baseline must pass. Check returns its problems, or none. */
interface SortationBaselineRule {
    fun check(item: SortationBaseline): MutableList<String>
}

data class SortationBaselineNameLengthRule(var maxLength: Int) : SortationBaselineRule {
    override fun check(item: SortationBaseline): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class SortationBaselineRequiredNameRule(var message: String) : SortationBaselineRule {
    override fun check(item: SortationBaseline): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class SortationBaselineWeightRangeRule(var min: Double, var max: Double) : SortationBaselineRule {
    override fun check(item: SortationBaseline): MutableList<String> {
        val value = item.weight
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("weight $value is below $min")
        }
        if (value > max) {
            problems.add("weight $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a sortation baseline. */
data class SortationBaselineValidator(var rules: MutableList<SortationBaselineRule>) {
    fun validate(item: SortationBaseline): MutableList<String> {
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

    fun requireValid(item: SortationBaseline) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<SortationBaseline>): MutableMap<String, MutableList<String>> {
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

fun defaultSortationBaselineValidator(): SortationBaselineValidator {
    return SortationBaselineValidator(
        mutableListOf<SortationBaselineRule>(
            SortationBaselineNameLengthRule(80),
            SortationBaselineRequiredNameRule("sortation baseline needs an entry"),
            SortationBaselineWeightRangeRule(2.2, 65.9),
        ),
    )
}
