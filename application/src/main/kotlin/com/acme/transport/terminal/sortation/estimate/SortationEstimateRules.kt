package com.acme.transport.terminal.sortation.estimate

/** A rule a sortation estimate must pass. Check returns its problems, or none. */
interface SortationEstimateRule {
    fun check(item: SortationEstimate): MutableList<String>
}

data class SortationEstimateRequiredDepotRule(var message: String) : SortationEstimateRule {
    override fun check(item: SortationEstimate): MutableList<String> {
        if (item.depot.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class SortationEstimateNameLengthRule(var maxLength: Int) : SortationEstimateRule {
    override fun check(item: SortationEstimate): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class SortationEstimateRequiredNameRule(var message: String) : SortationEstimateRule {
    override fun check(item: SortationEstimate): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class SortationEstimatePalletsRangeRule(var min: Int, var max: Int) : SortationEstimateRule {
    override fun check(item: SortationEstimate): MutableList<String> {
        val value = item.pallets
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("pallets $value is below $min")
        }
        if (value > max) {
            problems.add("pallets $value is above $max")
        }
        return problems
    }
}

data class SortationEstimateRateRangeRule(var min: Double, var max: Double) : SortationEstimateRule {
    override fun check(item: SortationEstimate): MutableList<String> {
        val value = item.rate
        if (value < min || value > max) {
            return mutableListOf<String>("rate $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a sortation estimate. */
data class SortationEstimateValidator(var rules: MutableList<SortationEstimateRule>) {
    fun validate(item: SortationEstimate): MutableList<String> {
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

    fun requireValid(item: SortationEstimate) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<SortationEstimate>): MutableMap<String, MutableList<String>> {
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

fun defaultSortationEstimateValidator(): SortationEstimateValidator {
    return SortationEstimateValidator(
        mutableListOf<SortationEstimateRule>(
            SortationEstimateRequiredDepotRule("sortation estimate needs a value"),
            SortationEstimateNameLengthRule(86400),
            SortationEstimateRequiredNameRule("sortation estimate needs this field"),
            SortationEstimatePalletsRangeRule(64, 80),
            SortationEstimateRateRangeRule(0.8, 494.7),
        ),
    )
}
