package com.acme.transport.crewing.availability.availabilityscore

/** A rule a capped availability rollup must pass. Check returns its problems, or none. */
interface CappedAvailabilityRollupRule {
    fun check(item: CappedAvailabilityRollup): MutableList<String>
}

data class CappedAvailabilityRollupMarginRangeRule(
    var min: Double,
    var max: Double,
) : CappedAvailabilityRollupRule {
    override fun check(item: CappedAvailabilityRollup): MutableList<String> {
        val value = item.margin
        if (value < min || value > max) {
            return mutableListOf<String>("margin $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class CappedAvailabilityRollupNameLengthRule(var maxLength: Int) : CappedAvailabilityRollupRule {
    override fun check(item: CappedAvailabilityRollup): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CappedAvailabilityRollupRateRangeRule(
    var min: Double,
    var max: Double,
) : CappedAvailabilityRollupRule {
    override fun check(item: CappedAvailabilityRollup): MutableList<String> {
        val value = item.rate
        if (value < min || value > max) {
            return mutableListOf<String>("rate $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a capped availability rollup. */
data class CappedAvailabilityRollupValidator(var rules: MutableList<CappedAvailabilityRollupRule>) {
    fun validate(item: CappedAvailabilityRollup): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: CappedAvailabilityRollup) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CappedAvailabilityRollup>): MutableMap<String, MutableList<String>> {
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

fun defaultCappedAvailabilityRollupValidator(): CappedAvailabilityRollupValidator {
    return CappedAvailabilityRollupValidator(
        mutableListOf<CappedAvailabilityRollupRule>(
            CappedAvailabilityRollupMarginRangeRule(8.0, 1282.5),
            CappedAvailabilityRollupNameLengthRule(86400),
            CappedAvailabilityRollupRateRangeRule(8.9, 2805.4),
        ),
    )
}
