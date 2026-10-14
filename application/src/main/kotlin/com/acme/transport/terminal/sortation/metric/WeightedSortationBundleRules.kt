package com.acme.transport.terminal.sortation.metric

/** A rule a weighted sortation bundle must pass. Check returns its problems, or none. */
interface WeightedSortationBundleRule {
    fun check(item: WeightedSortationBundle): MutableList<String>
}

data class WeightedSortationBundleDockLengthRule(var maxLength: Int) : WeightedSortationBundleRule {
    override fun check(item: WeightedSortationBundle): MutableList<String> {
        val size = item.dock.length
        if (size > maxLength) {
            return mutableListOf<String>("dock is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class WeightedSortationBundleRequiredCodeRule(var message: String) : WeightedSortationBundleRule {
    override fun check(item: WeightedSortationBundle): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class WeightedSortationBundleRequiredDockRule(var message: String) : WeightedSortationBundleRule {
    override fun check(item: WeightedSortationBundle): MutableList<String> {
        if (item.dock.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class WeightedSortationBundleFuelLitersRangeRule(
    var min: Double,
    var max: Double,
) : WeightedSortationBundleRule {
    override fun check(item: WeightedSortationBundle): MutableList<String> {
        val value = item.fuelLiters
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("fuel liters $value is below $min")
        }
        if (value > max) {
            problems.add("fuel liters $value is above $max")
        }
        return problems
    }
}

data class WeightedSortationBundleSourceLengthRule(var maxLength: Int) : WeightedSortationBundleRule {
    override fun check(item: WeightedSortationBundle): MutableList<String> {
        val size = item.source.length
        if (size > maxLength) {
            return mutableListOf<String>("source is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class WeightedSortationBundleCodePrefixRule(var prefix: String) : WeightedSortationBundleRule {
    override fun check(item: WeightedSortationBundle): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a weighted sortation bundle. */
data class WeightedSortationBundleValidator(var rules: MutableList<WeightedSortationBundleRule>) {
    fun validate(item: WeightedSortationBundle): MutableList<String> {
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

    fun requireValid(item: WeightedSortationBundle) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<WeightedSortationBundle>): MutableMap<String, MutableList<String>> {
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

fun defaultWeightedSortationBundleValidator(): WeightedSortationBundleValidator {
    return WeightedSortationBundleValidator(
        mutableListOf<WeightedSortationBundleRule>(
            WeightedSortationBundleDockLengthRule(120),
            WeightedSortationBundleRequiredCodeRule("weighted sortation bundle needs this field"),
            WeightedSortationBundleRequiredDockRule("weighted sortation bundle needs an entry"),
            WeightedSortationBundleFuelLitersRangeRule(2.0, 2549.7),
            WeightedSortationBundleSourceLengthRule(120),
            WeightedSortationBundleCodePrefixRule("X"),
        ),
    )
}
