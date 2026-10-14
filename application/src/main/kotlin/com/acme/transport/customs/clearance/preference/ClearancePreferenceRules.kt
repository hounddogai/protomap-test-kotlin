package com.acme.transport.customs.clearance.preference

/** A rule a clearance preference must pass. Check returns its problems, or none. */
interface ClearancePreferenceRule {
    fun check(item: ClearancePreference): MutableList<String>
}

data class ClearancePreferenceCapacityRangeRule(var min: Int, var max: Int) : ClearancePreferenceRule {
    override fun check(item: ClearancePreference): MutableList<String> {
        val value = item.capacity
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("capacity $value is below $min")
        }
        if (value > max) {
            problems.add("capacity $value is above $max")
        }
        return problems
    }
}

data class ClearancePreferenceUpdatedAtRangeRule(var min: Int, var max: Int) : ClearancePreferenceRule {
    override fun check(item: ClearancePreference): MutableList<String> {
        val value = item.updatedAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("updated at $value is below $min")
        }
        if (value > max) {
            problems.add("updated at $value is above $max")
        }
        return problems
    }
}

data class ClearancePreferenceRegionLengthRule(var maxLength: Int) : ClearancePreferenceRule {
    override fun check(item: ClearancePreference): MutableList<String> {
        val size = item.region.length
        if (size > maxLength) {
            return mutableListOf<String>("region is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a clearance preference. */
data class ClearancePreferenceValidator(var rules: MutableList<ClearancePreferenceRule>) {
    fun validate(item: ClearancePreference): MutableList<String> {
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

    fun requireValid(item: ClearancePreference) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ClearancePreference>): MutableMap<String, MutableList<String>> {
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

fun defaultClearancePreferenceValidator(): ClearancePreferenceValidator {
    return ClearancePreferenceValidator(
        mutableListOf<ClearancePreferenceRule>(
            ClearancePreferenceCapacityRangeRule(16, 32),
            ClearancePreferenceUpdatedAtRangeRule(1000, 86400),
            ClearancePreferenceRegionLengthRule(255),
        ),
    )
}
