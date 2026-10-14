package com.acme.transport.crewing.availability.profile

import com.acme.transport.kit.text.normalize

/** A rule a pending availability preference must pass. Check returns its problems, or none. */
interface PendingAvailabilityPreferenceRule {
    fun check(item: PendingAvailabilityPreference): MutableList<String>
}

data class PendingAvailabilityPreferenceWindowDaysRangeRule(
    var min: Int,
    var max: Int,
) : PendingAvailabilityPreferenceRule {
    override fun check(item: PendingAvailabilityPreference): MutableList<String> {
        val value = item.windowDays
        if (value < min || value > max) {
            return mutableListOf<String>("window days $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class PendingAvailabilityPreferenceRequiredCodeRule(
    var message: String,
) : PendingAvailabilityPreferenceRule {
    override fun check(item: PendingAvailabilityPreference): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PendingAvailabilityPreferenceAllowedExternalRefRule(
    var allowed: MutableList<String>,
) : PendingAvailabilityPreferenceRule {
    override fun check(item: PendingAvailabilityPreference): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.externalRef))) {
            return mutableListOf<String>("external ref ${item.externalRef} is stale")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a pending availability preference. */
data class PendingAvailabilityPreferenceValidator(var rules: MutableList<PendingAvailabilityPreferenceRule>) {
    fun validate(item: PendingAvailabilityPreference): MutableList<String> {
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

    fun requireValid(item: PendingAvailabilityPreference) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PendingAvailabilityPreference>): MutableMap<String, MutableList<String>> {
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

fun defaultPendingAvailabilityPreferenceValidator(): PendingAvailabilityPreferenceValidator {
    return PendingAvailabilityPreferenceValidator(
        mutableListOf<PendingAvailabilityPreferenceRule>(
            PendingAvailabilityPreferenceWindowDaysRangeRule(16, 64),
            PendingAvailabilityPreferenceRequiredCodeRule("pending availability preference needs this field"),
            PendingAvailabilityPreferenceAllowedExternalRefRule(mutableListOf<String>("lenient", "global")),
        ),
    )
}
