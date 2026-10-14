package com.acme.transport.crewing.availability.segment

/** A rule a primary availability baseline must pass. Check returns its problems, or none. */
interface PrimaryAvailabilityBaselineRule {
    fun check(item: PrimaryAvailabilityBaseline): MutableList<String>
}

data class PrimaryAvailabilityBaselineUnitLengthRule(var maxLength: Int) : PrimaryAvailabilityBaselineRule {
    override fun check(item: PrimaryAvailabilityBaseline): MutableList<String> {
        val size = item.unit.length
        if (size > maxLength) {
            return mutableListOf<String>("unit is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryAvailabilityBaselineLoadKgRangeRule(
    var min: Double,
    var max: Double,
) : PrimaryAvailabilityBaselineRule {
    override fun check(item: PrimaryAvailabilityBaseline): MutableList<String> {
        val value = item.loadKg
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("load kg $value is below $min")
        }
        if (value > max) {
            problems.add("load kg $value is above $max")
        }
        return problems
    }
}

data class PrimaryAvailabilityBaselineRequiredCodeRule(var message: String) : PrimaryAvailabilityBaselineRule {
    override fun check(item: PrimaryAvailabilityBaseline): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a primary availability baseline. */
data class PrimaryAvailabilityBaselineValidator(var rules: MutableList<PrimaryAvailabilityBaselineRule>) {
    fun validate(item: PrimaryAvailabilityBaseline): MutableList<String> {
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

    fun requireValid(item: PrimaryAvailabilityBaseline) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PrimaryAvailabilityBaseline>): MutableMap<String, MutableList<String>> {
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

fun defaultPrimaryAvailabilityBaselineValidator(): PrimaryAvailabilityBaselineValidator {
    return PrimaryAvailabilityBaselineValidator(
        mutableListOf<PrimaryAvailabilityBaselineRule>(
            PrimaryAvailabilityBaselineUnitLengthRule(120),
            PrimaryAvailabilityBaselineLoadKgRangeRule(7.1, 3839.7),
            PrimaryAvailabilityBaselineRequiredCodeRule("primary availability baseline needs an entry"),
        ),
    )
}
