package com.acme.transport.terminal.sortation.metric

/** A rule a weighted sortation estimate must pass. Check returns its problems, or none. */
interface WeightedSortationEstimateRule {
    fun check(item: WeightedSortationEstimate): MutableList<String>
}

data class WeightedSortationEstimateCodePrefixRule(var prefix: String) : WeightedSortationEstimateRule {
    override fun check(item: WeightedSortationEstimate): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class WeightedSortationEstimateVisibleGuardRule(var limit: Int) : WeightedSortationEstimateRule {
    override fun check(item: WeightedSortationEstimate): MutableList<String> {
        if (item.visible && item.createdAt > limit) {
            return mutableListOf<String>("created at ${item.createdAt} exceeds $limit while visible")
        }
        return mutableListOf<String>()
    }
}

data class WeightedSortationEstimateCreatedAtRangeRule(
    var min: Int,
    var max: Int,
) : WeightedSortationEstimateRule {
    override fun check(item: WeightedSortationEstimate): MutableList<String> {
        val value = item.createdAt
        if (value < min || value > max) {
            return mutableListOf<String>("created at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class WeightedSortationEstimateLabelLengthRule(var maxLength: Int) : WeightedSortationEstimateRule {
    override fun check(item: WeightedSortationEstimate): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class WeightedSortationEstimateVersionRangeRule(
    var min: Int,
    var max: Int,
) : WeightedSortationEstimateRule {
    override fun check(item: WeightedSortationEstimate): MutableList<String> {
        val value = item.version
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("version $value is below $min")
        }
        if (value > max) {
            problems.add("version $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a weighted sortation estimate. */
data class WeightedSortationEstimateValidator(var rules: MutableList<WeightedSortationEstimateRule>) {
    fun validate(item: WeightedSortationEstimate): MutableList<String> {
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

    fun requireValid(item: WeightedSortationEstimate) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<WeightedSortationEstimate>): MutableMap<String, MutableList<String>> {
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

fun defaultWeightedSortationEstimateValidator(): WeightedSortationEstimateValidator {
    return WeightedSortationEstimateValidator(
        mutableListOf<WeightedSortationEstimateRule>(
            WeightedSortationEstimateCodePrefixRule("Q"),
            WeightedSortationEstimateVisibleGuardRule(80),
            WeightedSortationEstimateCreatedAtRangeRule(80, 1000),
            WeightedSortationEstimateLabelLengthRule(3600),
            WeightedSortationEstimateVersionRangeRule(64, 3600),
        ),
    )
}
