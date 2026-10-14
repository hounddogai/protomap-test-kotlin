package com.acme.transport.safety.incident.digest

import com.acme.transport.kit.text.normalize

/** A rule a tiered incident preference must pass. Check returns its problems, or none. */
interface TieredIncidentPreferenceRule {
    fun check(item: TieredIncidentPreference): MutableList<String>
}

data class TieredIncidentPreferencePinnedGuardRule(var limit: Double) : TieredIncidentPreferenceRule {
    override fun check(item: TieredIncidentPreference): MutableList<String> {
        if (item.pinned && item.score > limit) {
            return mutableListOf<String>("score ${item.score} exceeds $limit while pinned")
        }
        return mutableListOf<String>()
    }
}

data class TieredIncidentPreferenceAllowedCodeRule(
    var allowed: MutableList<String>,
) : TieredIncidentPreferenceRule {
    override fun check(item: TieredIncidentPreference): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is above maximum")
        }
        return mutableListOf<String>()
    }
}

data class TieredIncidentPreferenceCodePrefixRule(var prefix: String) : TieredIncidentPreferenceRule {
    override fun check(item: TieredIncidentPreference): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class TieredIncidentPreferenceLabelLengthRule(var maxLength: Int) : TieredIncidentPreferenceRule {
    override fun check(item: TieredIncidentPreference): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a tiered incident preference. */
data class TieredIncidentPreferenceValidator(var rules: MutableList<TieredIncidentPreferenceRule>) {
    fun validate(item: TieredIncidentPreference): MutableList<String> {
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

    fun requireValid(item: TieredIncidentPreference) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TieredIncidentPreference>): MutableMap<String, MutableList<String>> {
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

fun defaultTieredIncidentPreferenceValidator(): TieredIncidentPreferenceValidator {
    return TieredIncidentPreferenceValidator(
        mutableListOf<TieredIncidentPreferenceRule>(
            TieredIncidentPreferencePinnedGuardRule(5.1),
            TieredIncidentPreferenceAllowedCodeRule(mutableListOf<String>("primary", "regional", "tiered")),
            TieredIncidentPreferenceCodePrefixRule("REF"),
            TieredIncidentPreferenceLabelLengthRule(86400),
        ),
    )
}
