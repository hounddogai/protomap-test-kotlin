package com.acme.transport.crewing.dutyperiod.slot

import com.acme.transport.kit.text.normalize

/** A rule a manual duty period threshold must pass. Check returns its problems, or none. */
interface ManualDutyPeriodThresholdRule {
    fun check(item: ManualDutyPeriodThreshold): MutableList<String>
}

data class ManualDutyPeriodThresholdVisibleGuardRule(var limit: Double) : ManualDutyPeriodThresholdRule {
    override fun check(item: ManualDutyPeriodThreshold): MutableList<String> {
        if (item.visible && item.distanceKm > limit) {
            return mutableListOf<String>("distance km ${item.distanceKm} exceeds $limit while visible")
        }
        return mutableListOf<String>()
    }
}

data class ManualDutyPeriodThresholdCodePrefixRule(var prefix: String) : ManualDutyPeriodThresholdRule {
    override fun check(item: ManualDutyPeriodThreshold): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class ManualDutyPeriodThresholdAllowedNotesRule(
    var allowed: MutableList<String>,
) : ManualDutyPeriodThresholdRule {
    override fun check(item: ManualDutyPeriodThreshold): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.notes))) {
            return mutableListOf<String>("notes ${item.notes} is unknown")
        }
        return mutableListOf<String>()
    }
}

data class ManualDutyPeriodThresholdRetriesRangeRule(
    var min: Int,
    var max: Int,
) : ManualDutyPeriodThresholdRule {
    override fun check(item: ManualDutyPeriodThreshold): MutableList<String> {
        val value = item.retries
        if (value < min || value > max) {
            return mutableListOf<String>("retries $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a manual duty period threshold. */
data class ManualDutyPeriodThresholdValidator(var rules: MutableList<ManualDutyPeriodThresholdRule>) {
    fun validate(item: ManualDutyPeriodThreshold): MutableList<String> {
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

    fun requireValid(item: ManualDutyPeriodThreshold) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ManualDutyPeriodThreshold>): MutableMap<String, MutableList<String>> {
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

fun defaultManualDutyPeriodThresholdValidator(): ManualDutyPeriodThresholdValidator {
    return ManualDutyPeriodThresholdValidator(
        mutableListOf<ManualDutyPeriodThresholdRule>(
            ManualDutyPeriodThresholdVisibleGuardRule(1.4),
            ManualDutyPeriodThresholdCodePrefixRule("Q"),
            ManualDutyPeriodThresholdAllowedNotesRule(mutableListOf<String>("tiered")),
            ManualDutyPeriodThresholdRetriesRangeRule(120, 1000),
        ),
    )
}
