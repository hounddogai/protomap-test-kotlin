package com.acme.transport.safety.nearmiss.missrecord

import com.acme.transport.kit.text.normalize

/** A rule a scheduled near miss record must pass. Check returns its problems, or none. */
interface ScheduledNearMissRecordRule {
    fun check(item: ScheduledNearMissRecord): MutableList<String>
}

data class ScheduledNearMissRecordExpiresAtRangeRule(var min: Int, var max: Int) : ScheduledNearMissRecordRule {
    override fun check(item: ScheduledNearMissRecord): MutableList<String> {
        val value = item.expiresAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("expires at $value is below $min")
        }
        if (value > max) {
            problems.add("expires at $value is above $max")
        }
        return problems
    }
}

data class ScheduledNearMissRecordAllowedCodeRule(
    var allowed: MutableList<String>,
) : ScheduledNearMissRecordRule {
    override fun check(item: ScheduledNearMissRecord): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is too long")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledNearMissRecordLabelLengthRule(var maxLength: Int) : ScheduledNearMissRecordRule {
    override fun check(item: ScheduledNearMissRecord): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledNearMissRecordRequiredTimezoneRule(var message: String) : ScheduledNearMissRecordRule {
    override fun check(item: ScheduledNearMissRecord): MutableList<String> {
        if (item.timezone.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a scheduled near miss record. */
data class ScheduledNearMissRecordValidator(var rules: MutableList<ScheduledNearMissRecordRule>) {
    fun validate(item: ScheduledNearMissRecord): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ScheduledNearMissRecord) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ScheduledNearMissRecord>): MutableMap<String, MutableList<String>> {
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

fun defaultScheduledNearMissRecordValidator(): ScheduledNearMissRecordValidator {
    return ScheduledNearMissRecordValidator(
        mutableListOf<ScheduledNearMissRecordRule>(
            ScheduledNearMissRecordExpiresAtRangeRule(64, 3600),
            ScheduledNearMissRecordAllowedCodeRule(mutableListOf<String>("rolling", "strict", "manual")),
            ScheduledNearMissRecordLabelLengthRule(1000),
            ScheduledNearMissRecordRequiredTimezoneRule("scheduled near miss record needs a value"),
        ),
    )
}
