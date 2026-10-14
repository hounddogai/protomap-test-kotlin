package com.acme.transport.customs.clearance.rollup

/** A rule a tiered clearance preference must pass. Check returns its problems, or none. */
interface TieredClearancePreferenceRule {
    fun check(item: TieredClearancePreference): MutableList<String>
}

data class TieredClearancePreferenceEndsAtRangeRule(
    var min: Int,
    var max: Int,
) : TieredClearancePreferenceRule {
    override fun check(item: TieredClearancePreference): MutableList<String> {
        val value = item.endsAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("ends at $value is below $min")
        }
        if (value > max) {
            problems.add("ends at $value is above $max")
        }
        return problems
    }
}

data class TieredClearancePreferenceLabelLengthRule(var maxLength: Int) : TieredClearancePreferenceRule {
    override fun check(item: TieredClearancePreference): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TieredClearancePreferenceRequiredTrailerIdRule(var message: String) : TieredClearancePreferenceRule {
    override fun check(item: TieredClearancePreference): MutableList<String> {
        if (item.trailerId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TieredClearancePreferenceCapacityRangeRule(
    var min: Int,
    var max: Int,
) : TieredClearancePreferenceRule {
    override fun check(item: TieredClearancePreference): MutableList<String> {
        val value = item.capacity
        if (value < min || value > max) {
            return mutableListOf<String>("capacity $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class TieredClearancePreferenceRequiredRegionRule(var message: String) : TieredClearancePreferenceRule {
    override fun check(item: TieredClearancePreference): MutableList<String> {
        if (item.region.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TieredClearancePreferenceCodeLengthRule(var maxLength: Int) : TieredClearancePreferenceRule {
    override fun check(item: TieredClearancePreference): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a tiered clearance preference. */
data class TieredClearancePreferenceValidator(var rules: MutableList<TieredClearancePreferenceRule>) {
    fun validate(item: TieredClearancePreference): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: TieredClearancePreference) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TieredClearancePreference>): MutableMap<String, MutableList<String>> {
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

fun defaultTieredClearancePreferenceValidator(): TieredClearancePreferenceValidator {
    return TieredClearancePreferenceValidator(
        mutableListOf<TieredClearancePreferenceRule>(
            TieredClearancePreferenceEndsAtRangeRule(64, 86400),
            TieredClearancePreferenceLabelLengthRule(64),
            TieredClearancePreferenceRequiredTrailerIdRule("tiered clearance preference needs an entry"),
            TieredClearancePreferenceCapacityRangeRule(255, 1000),
            TieredClearancePreferenceRequiredRegionRule("tiered clearance preference needs an entry"),
            TieredClearancePreferenceCodeLengthRule(64),
        ),
    )
}
