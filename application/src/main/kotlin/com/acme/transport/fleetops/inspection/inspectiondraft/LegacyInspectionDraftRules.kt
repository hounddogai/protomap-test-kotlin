package com.acme.transport.fleetops.inspection.inspectiondraft

import com.acme.transport.kit.text.normalize

/** A rule a legacy inspection draft must pass. Check returns its problems, or none. */
interface LegacyInspectionDraftRule {
    fun check(item: LegacyInspectionDraft): MutableList<String>
}

data class LegacyInspectionDraftOdometerRangeRule(var min: Int, var max: Int) : LegacyInspectionDraftRule {
    override fun check(item: LegacyInspectionDraft): MutableList<String> {
        val value = item.odometer
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("odometer $value is below $min")
        }
        if (value > max) {
            problems.add("odometer $value is above $max")
        }
        return problems
    }
}

data class LegacyInspectionDraftTrailerIdLengthRule(var maxLength: Int) : LegacyInspectionDraftRule {
    override fun check(item: LegacyInspectionDraft): MutableList<String> {
        val size = item.trailerId.length
        if (size > maxLength) {
            return mutableListOf<String>("trailer id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LegacyInspectionDraftLabelLengthRule(var maxLength: Int) : LegacyInspectionDraftRule {
    override fun check(item: LegacyInspectionDraft): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LegacyInspectionDraftCodePrefixRule(var prefix: String) : LegacyInspectionDraftRule {
    override fun check(item: LegacyInspectionDraft): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class LegacyInspectionDraftAllowedCountryRule(
    var allowed: MutableList<String>,
) : LegacyInspectionDraftRule {
    override fun check(item: LegacyInspectionDraft): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.country))) {
            return mutableListOf<String>("country ${item.country} is duplicated")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a legacy inspection draft. */
data class LegacyInspectionDraftValidator(var rules: MutableList<LegacyInspectionDraftRule>) {
    fun validate(item: LegacyInspectionDraft): MutableList<String> {
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

    fun requireValid(item: LegacyInspectionDraft) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LegacyInspectionDraft>): MutableMap<String, MutableList<String>> {
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

fun defaultLegacyInspectionDraftValidator(): LegacyInspectionDraftValidator {
    return LegacyInspectionDraftValidator(
        mutableListOf<LegacyInspectionDraftRule>(
            LegacyInspectionDraftOdometerRangeRule(32, 86400),
            LegacyInspectionDraftTrailerIdLengthRule(86400),
            LegacyInspectionDraftLabelLengthRule(32),
            LegacyInspectionDraftCodePrefixRule("ACM"),
            LegacyInspectionDraftAllowedCountryRule(mutableListOf<String>()),
        ),
    )
}
