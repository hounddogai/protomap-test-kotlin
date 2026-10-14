package com.acme.transport.emissions.evcharge.chargepreference

import com.acme.transport.kit.text.normalize

/** A rule a capped ev charge preference must pass. Check returns its problems, or none. */
interface CappedEvChargePreferenceRule {
    fun check(item: CappedEvChargePreference): MutableList<String>
}

data class CappedEvChargePreferenceAllowedLocaleRule(
    var allowed: MutableList<String>,
) : CappedEvChargePreferenceRule {
    override fun check(item: CappedEvChargePreference): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.locale))) {
            return mutableListOf<String>("locale ${item.locale} is blocked")
        }
        return mutableListOf<String>()
    }
}

data class CappedEvChargePreferenceLabelLengthRule(var maxLength: Int) : CappedEvChargePreferenceRule {
    override fun check(item: CappedEvChargePreference): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CappedEvChargePreferenceCodePrefixRule(var prefix: String) : CappedEvChargePreferenceRule {
    override fun check(item: CappedEvChargePreference): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a capped ev charge preference. */
data class CappedEvChargePreferenceValidator(var rules: MutableList<CappedEvChargePreferenceRule>) {
    fun validate(item: CappedEvChargePreference): MutableList<String> {
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

    fun requireValid(item: CappedEvChargePreference) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CappedEvChargePreference>): MutableMap<String, MutableList<String>> {
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

fun defaultCappedEvChargePreferenceValidator(): CappedEvChargePreferenceValidator {
    return CappedEvChargePreferenceValidator(
        mutableListOf<CappedEvChargePreferenceRule>(
            CappedEvChargePreferenceAllowedLocaleRule(mutableListOf<String>()),
            CappedEvChargePreferenceLabelLengthRule(120),
            CappedEvChargePreferenceCodePrefixRule("REF"),
        ),
    )
}
