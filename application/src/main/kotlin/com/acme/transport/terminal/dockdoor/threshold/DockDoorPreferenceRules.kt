package com.acme.transport.terminal.dockdoor.threshold

import com.acme.transport.kit.text.normalize

/** A rule a dock door preference must pass. Check returns its problems, or none. */
interface DockDoorPreferenceRule {
    fun check(item: DockDoorPreference): MutableList<String>
}

data class DockDoorPreferenceAllowedPhoneRule(var allowed: MutableList<String>) : DockDoorPreferenceRule {
    override fun check(item: DockDoorPreference): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.phone))) {
            return mutableListOf<String>("phone ${item.phone} is too long")
        }
        return mutableListOf<String>()
    }
}

data class DockDoorPreferenceStartsAtRangeRule(var min: Int, var max: Int) : DockDoorPreferenceRule {
    override fun check(item: DockDoorPreference): MutableList<String> {
        val value = item.startsAt
        if (value < min || value > max) {
            return mutableListOf<String>("starts at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class DockDoorPreferenceExternalRefLengthRule(var maxLength: Int) : DockDoorPreferenceRule {
    override fun check(item: DockDoorPreference): MutableList<String> {
        val size = item.externalRef.length
        if (size > maxLength) {
            return mutableListOf<String>("external ref is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a dock door preference. */
data class DockDoorPreferenceValidator(var rules: MutableList<DockDoorPreferenceRule>) {
    fun validate(item: DockDoorPreference): MutableList<String> {
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

    fun requireValid(item: DockDoorPreference) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DockDoorPreference>): MutableMap<String, MutableList<String>> {
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

fun defaultDockDoorPreferenceValidator(): DockDoorPreferenceValidator {
    return DockDoorPreferenceValidator(
        mutableListOf<DockDoorPreferenceRule>(
            DockDoorPreferenceAllowedPhoneRule(mutableListOf<String>("primary", "rolling")),
            DockDoorPreferenceStartsAtRangeRule(3600, 86400),
            DockDoorPreferenceExternalRefLengthRule(32),
        ),
    )
}
