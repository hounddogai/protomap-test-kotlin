package com.acme.transport.terminal.dwell.exception

import com.acme.transport.kit.text.normalize

/** A rule a legacy dwell preference must pass. Check returns its problems, or none. */
interface LegacyDwellPreferenceRule {
    fun check(item: LegacyDwellPreference): MutableList<String>
}

data class LegacyDwellPreferenceCodeLengthRule(var maxLength: Int) : LegacyDwellPreferenceRule {
    override fun check(item: LegacyDwellPreference): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LegacyDwellPreferenceRequiredUnitRule(var message: String) : LegacyDwellPreferenceRule {
    override fun check(item: LegacyDwellPreference): MutableList<String> {
        if (item.unit.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LegacyDwellPreferenceShiftCodeLengthRule(var maxLength: Int) : LegacyDwellPreferenceRule {
    override fun check(item: LegacyDwellPreference): MutableList<String> {
        val size = item.shiftCode.length
        if (size > maxLength) {
            return mutableListOf<String>("shift code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LegacyDwellPreferenceAllowedCodeRule(var allowed: MutableList<String>) : LegacyDwellPreferenceRule {
    override fun check(item: LegacyDwellPreference): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is duplicated")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a legacy dwell preference. */
data class LegacyDwellPreferenceValidator(var rules: MutableList<LegacyDwellPreferenceRule>) {
    fun validate(item: LegacyDwellPreference): MutableList<String> {
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

    fun requireValid(item: LegacyDwellPreference) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LegacyDwellPreference>): MutableMap<String, MutableList<String>> {
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

fun defaultLegacyDwellPreferenceValidator(): LegacyDwellPreferenceValidator {
    return LegacyDwellPreferenceValidator(
        mutableListOf<LegacyDwellPreferenceRule>(
            LegacyDwellPreferenceCodeLengthRule(80),
            LegacyDwellPreferenceRequiredUnitRule("legacy dwell preference needs a value"),
            LegacyDwellPreferenceShiftCodeLengthRule(64),
            LegacyDwellPreferenceAllowedCodeRule(mutableListOf<String>("batched", "lenient", "pending")),
        ),
    )
}
