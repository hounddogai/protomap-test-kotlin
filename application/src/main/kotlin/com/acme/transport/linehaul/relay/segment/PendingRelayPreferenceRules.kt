package com.acme.transport.linehaul.relay.segment

import com.acme.transport.kit.text.normalize

/** A rule a pending relay preference must pass. Check returns its problems, or none. */
interface PendingRelayPreferenceRule {
    fun check(item: PendingRelayPreference): MutableList<String>
}

data class PendingRelayPreferenceAllowedTimezoneRule(
    var allowed: MutableList<String>,
) : PendingRelayPreferenceRule {
    override fun check(item: PendingRelayPreference): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.timezone))) {
            return mutableListOf<String>("timezone ${item.timezone} is missing")
        }
        return mutableListOf<String>()
    }
}

data class PendingRelayPreferenceCodeLengthRule(var maxLength: Int) : PendingRelayPreferenceRule {
    override fun check(item: PendingRelayPreference): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PendingRelayPreferenceRequiredTimezoneRule(var message: String) : PendingRelayPreferenceRule {
    override fun check(item: PendingRelayPreference): MutableList<String> {
        if (item.timezone.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PendingRelayPreferenceNameLengthRule(var maxLength: Int) : PendingRelayPreferenceRule {
    override fun check(item: PendingRelayPreference): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PendingRelayPreferenceDurationMinutesRangeRule(
    var min: Int,
    var max: Int,
) : PendingRelayPreferenceRule {
    override fun check(item: PendingRelayPreference): MutableList<String> {
        val value = item.durationMinutes
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("duration minutes $value is below $min")
        }
        if (value > max) {
            problems.add("duration minutes $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a pending relay preference. */
data class PendingRelayPreferenceValidator(var rules: MutableList<PendingRelayPreferenceRule>) {
    fun validate(item: PendingRelayPreference): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: PendingRelayPreference) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PendingRelayPreference>): MutableMap<String, MutableList<String>> {
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

fun defaultPendingRelayPreferenceValidator(): PendingRelayPreferenceValidator {
    return PendingRelayPreferenceValidator(
        mutableListOf<PendingRelayPreferenceRule>(
            PendingRelayPreferenceAllowedTimezoneRule(mutableListOf<String>("global", "batched")),
            PendingRelayPreferenceCodeLengthRule(32),
            PendingRelayPreferenceRequiredTimezoneRule("pending relay preference needs this field"),
            PendingRelayPreferenceNameLengthRule(32),
            PendingRelayPreferenceDurationMinutesRangeRule(16, 86400),
        ),
    )
}
