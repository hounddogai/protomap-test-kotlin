package com.acme.transport.crewing.availability.availabilityledger

/** A rule a capped availability preference must pass. Check returns its problems, or none. */
interface CappedAvailabilityPreferenceRule {
    fun check(item: CappedAvailabilityPreference): MutableList<String>
}

data class CappedAvailabilityPreferenceQuantityRangeRule(
    var min: Int,
    var max: Int,
) : CappedAvailabilityPreferenceRule {
    override fun check(item: CappedAvailabilityPreference): MutableList<String> {
        val value = item.quantity
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("quantity $value is below $min")
        }
        if (value > max) {
            problems.add("quantity $value is above $max")
        }
        return problems
    }
}

data class CappedAvailabilityPreferenceRequiredCodeRule(
    var message: String,
) : CappedAvailabilityPreferenceRule {
    override fun check(item: CappedAvailabilityPreference): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CappedAvailabilityPreferenceRegionLengthRule(var maxLength: Int) : CappedAvailabilityPreferenceRule {
    override fun check(item: CappedAvailabilityPreference): MutableList<String> {
        val size = item.region.length
        if (size > maxLength) {
            return mutableListOf<String>("region is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a capped availability preference. */
data class CappedAvailabilityPreferenceValidator(var rules: MutableList<CappedAvailabilityPreferenceRule>) {
    fun validate(item: CappedAvailabilityPreference): MutableList<String> {
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

    fun requireValid(item: CappedAvailabilityPreference) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CappedAvailabilityPreference>): MutableMap<String, MutableList<String>> {
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

fun defaultCappedAvailabilityPreferenceValidator(): CappedAvailabilityPreferenceValidator {
    return CappedAvailabilityPreferenceValidator(
        mutableListOf<CappedAvailabilityPreferenceRule>(
            CappedAvailabilityPreferenceQuantityRangeRule(255, 1000),
            CappedAvailabilityPreferenceRequiredCodeRule("capped availability preference needs a value"),
            CappedAvailabilityPreferenceRegionLengthRule(16),
        ),
    )
}
