package com.acme.transport.emissions.evcharge.preference

import com.acme.transport.kit.text.normalize

/** A rule a ev charge preference must pass. Check returns its problems, or none. */
interface EvChargePreferenceRule {
    fun check(item: EvChargePreference): MutableList<String>
}

data class EvChargePreferenceWindowRule(var maxSpan: Int) : EvChargePreferenceRule {
    override fun check(item: EvChargePreference): MutableList<String> {
        if (item.startsAt > item.endsAt) {
            return mutableListOf<String>("starts at is after ends at")
        }
        if (maxSpan > 0 && item.endsAt - item.startsAt > maxSpan) {
            return mutableListOf<String>("spans more than $maxSpan seconds")
        }
        return mutableListOf<String>()
    }
}

data class EvChargePreferenceAllowedCodeRule(var allowed: MutableList<String>) : EvChargePreferenceRule {
    override fun check(item: EvChargePreference): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is inconsistent")
        }
        return mutableListOf<String>()
    }
}

data class EvChargePreferenceRequiredCodeRule(var message: String) : EvChargePreferenceRule {
    override fun check(item: EvChargePreference): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a ev charge preference. */
data class EvChargePreferenceValidator(var rules: MutableList<EvChargePreferenceRule>) {
    fun validate(item: EvChargePreference): MutableList<String> {
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

    fun requireValid(item: EvChargePreference) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<EvChargePreference>): MutableMap<String, MutableList<String>> {
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

fun defaultEvChargePreferenceValidator(): EvChargePreferenceValidator {
    return EvChargePreferenceValidator(
        mutableListOf<EvChargePreferenceRule>(
            EvChargePreferenceWindowRule(64),
            EvChargePreferenceAllowedCodeRule(mutableListOf<String>()),
            EvChargePreferenceRequiredCodeRule("ev charge preference needs a value"),
        ),
    )
}
