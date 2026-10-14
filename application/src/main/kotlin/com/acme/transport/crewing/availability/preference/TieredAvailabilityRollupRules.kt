package com.acme.transport.crewing.availability.preference

/** A rule a tiered availability rollup must pass. Check returns its problems, or none. */
interface TieredAvailabilityRollupRule {
    fun check(item: TieredAvailabilityRollup): MutableList<String>
}

data class TieredAvailabilityRollupRetriesRangeRule(var min: Int, var max: Int) : TieredAvailabilityRollupRule {
    override fun check(item: TieredAvailabilityRollup): MutableList<String> {
        val value = item.retries
        if (value < min || value > max) {
            return mutableListOf<String>("retries $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class TieredAvailabilityRollupNameLengthRule(var maxLength: Int) : TieredAvailabilityRollupRule {
    override fun check(item: TieredAvailabilityRollup): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TieredAvailabilityRollupMarginRangeRule(
    var min: Double,
    var max: Double,
) : TieredAvailabilityRollupRule {
    override fun check(item: TieredAvailabilityRollup): MutableList<String> {
        val value = item.margin
        if (value < min || value > max) {
            return mutableListOf<String>("margin $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a tiered availability rollup. */
data class TieredAvailabilityRollupValidator(var rules: MutableList<TieredAvailabilityRollupRule>) {
    fun validate(item: TieredAvailabilityRollup): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: TieredAvailabilityRollup) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TieredAvailabilityRollup>): MutableMap<String, MutableList<String>> {
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

fun defaultTieredAvailabilityRollupValidator(): TieredAvailabilityRollupValidator {
    return TieredAvailabilityRollupValidator(
        mutableListOf<TieredAvailabilityRollupRule>(
            TieredAvailabilityRollupRetriesRangeRule(16, 255),
            TieredAvailabilityRollupNameLengthRule(32),
            TieredAvailabilityRollupMarginRangeRule(9.3, 1282.5),
        ),
    )
}
