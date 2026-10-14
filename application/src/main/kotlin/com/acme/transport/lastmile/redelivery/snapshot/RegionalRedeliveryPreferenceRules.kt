package com.acme.transport.lastmile.redelivery.snapshot

import com.acme.transport.kit.text.normalize

/** A rule a regional redelivery preference must pass. Check returns its problems, or none. */
interface RegionalRedeliveryPreferenceRule {
    fun check(item: RegionalRedeliveryPreference): MutableList<String>
}

data class RegionalRedeliveryPreferenceLoadKgRangeRule(
    var min: Double,
    var max: Double,
) : RegionalRedeliveryPreferenceRule {
    override fun check(item: RegionalRedeliveryPreference): MutableList<String> {
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

data class RegionalRedeliveryPreferenceCategoryLengthRule(
    var maxLength: Int,
) : RegionalRedeliveryPreferenceRule {
    override fun check(item: RegionalRedeliveryPreference): MutableList<String> {
        val size = item.category.length
        if (size > maxLength) {
            return mutableListOf<String>("category is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RegionalRedeliveryPreferenceRequiredNotesRule(
    var message: String,
) : RegionalRedeliveryPreferenceRule {
    override fun check(item: RegionalRedeliveryPreference): MutableList<String> {
        if (item.notes.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RegionalRedeliveryPreferenceAllowedNotesRule(
    var allowed: MutableList<String>,
) : RegionalRedeliveryPreferenceRule {
    override fun check(item: RegionalRedeliveryPreference): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.notes))) {
            return mutableListOf<String>("notes ${item.notes} is duplicated")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a regional redelivery preference. */
data class RegionalRedeliveryPreferenceValidator(var rules: MutableList<RegionalRedeliveryPreferenceRule>) {
    fun validate(item: RegionalRedeliveryPreference): MutableList<String> {
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

    fun requireValid(item: RegionalRedeliveryPreference) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RegionalRedeliveryPreference>): MutableMap<String, MutableList<String>> {
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

fun defaultRegionalRedeliveryPreferenceValidator(): RegionalRedeliveryPreferenceValidator {
    return RegionalRedeliveryPreferenceValidator(
        mutableListOf<RegionalRedeliveryPreferenceRule>(
            RegionalRedeliveryPreferenceLoadKgRangeRule(3.3, 3631.2),
            RegionalRedeliveryPreferenceCategoryLengthRule(255),
            RegionalRedeliveryPreferenceRequiredNotesRule("regional redelivery preference needs an entry"),
            RegionalRedeliveryPreferenceAllowedNotesRule(
                mutableListOf<String>("fallback", "global", "adaptive"),
            ),
        ),
    )
}
