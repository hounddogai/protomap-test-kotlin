package com.acme.transport.terminal.sortation.recorditem

/** A rule a weighted sortation override must pass. Check returns its problems, or none. */
interface WeightedSortationOverrideRule {
    fun check(item: WeightedSortationOverride): MutableList<String>
}

data class WeightedSortationOverrideRequiredCodeRule(var message: String) : WeightedSortationOverrideRule {
    override fun check(item: WeightedSortationOverride): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class WeightedSortationOverrideCreatedAtRangeRule(
    var min: Int,
    var max: Int,
) : WeightedSortationOverrideRule {
    override fun check(item: WeightedSortationOverride): MutableList<String> {
        val value = item.createdAt
        if (value < min || value > max) {
            return mutableListOf<String>("created at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class WeightedSortationOverrideCurrencyLengthRule(var maxLength: Int) : WeightedSortationOverrideRule {
    override fun check(item: WeightedSortationOverride): MutableList<String> {
        val size = item.currency.length
        if (size > maxLength) {
            return mutableListOf<String>("currency is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class WeightedSortationOverrideRequiredVehicleIdRule(var message: String) : WeightedSortationOverrideRule {
    override fun check(item: WeightedSortationOverride): MutableList<String> {
        if (item.vehicleId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class WeightedSortationOverrideWindowDaysRangeRule(
    var min: Int,
    var max: Int,
) : WeightedSortationOverrideRule {
    override fun check(item: WeightedSortationOverride): MutableList<String> {
        val value = item.windowDays
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("window days $value is below $min")
        }
        if (value > max) {
            problems.add("window days $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a weighted sortation override. */
data class WeightedSortationOverrideValidator(var rules: MutableList<WeightedSortationOverrideRule>) {
    fun validate(item: WeightedSortationOverride): MutableList<String> {
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

    fun requireValid(item: WeightedSortationOverride) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<WeightedSortationOverride>): MutableMap<String, MutableList<String>> {
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

fun defaultWeightedSortationOverrideValidator(): WeightedSortationOverrideValidator {
    return WeightedSortationOverrideValidator(
        mutableListOf<WeightedSortationOverrideRule>(
            WeightedSortationOverrideRequiredCodeRule("weighted sortation override needs an entry"),
            WeightedSortationOverrideCreatedAtRangeRule(120, 255),
            WeightedSortationOverrideCurrencyLengthRule(16),
            WeightedSortationOverrideRequiredVehicleIdRule("weighted sortation override needs this field"),
            WeightedSortationOverrideWindowDaysRangeRule(1000, 86400),
        ),
    )
}
