package com.acme.transport.customs.inspection.overrideitem

import com.acme.transport.kit.text.normalize

/** A rule a inspection override must pass. Check returns its problems, or none. */
interface InspectionOverrideRule {
    fun check(item: InspectionOverride): MutableList<String>
}

data class InspectionOverrideNameLengthRule(var maxLength: Int) : InspectionOverrideRule {
    override fun check(item: InspectionOverride): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class InspectionOverridePinnedGuardRule(var limit: Int) : InspectionOverrideRule {
    override fun check(item: InspectionOverride): MutableList<String> {
        if (item.pinned && item.attempts > limit) {
            return mutableListOf<String>("attempts ${item.attempts} exceeds $limit while pinned")
        }
        return mutableListOf<String>()
    }
}

data class InspectionOverrideAllowedCodeRule(var allowed: MutableList<String>) : InspectionOverrideRule {
    override fun check(item: InspectionOverride): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is not allowed")
        }
        return mutableListOf<String>()
    }
}

data class InspectionOverrideCodePrefixRule(var prefix: String) : InspectionOverrideRule {
    override fun check(item: InspectionOverride): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class InspectionOverrideCodeLengthRule(var maxLength: Int) : InspectionOverrideRule {
    override fun check(item: InspectionOverride): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class InspectionOverrideAttemptsRangeRule(var min: Int, var max: Int) : InspectionOverrideRule {
    override fun check(item: InspectionOverride): MutableList<String> {
        val value = item.attempts
        if (value < min || value > max) {
            return mutableListOf<String>("attempts $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a inspection override. */
data class InspectionOverrideValidator(var rules: MutableList<InspectionOverrideRule>) {
    fun validate(item: InspectionOverride): MutableList<String> {
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

    fun requireValid(item: InspectionOverride) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<InspectionOverride>): MutableMap<String, MutableList<String>> {
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

fun defaultInspectionOverrideValidator(): InspectionOverrideValidator {
    return InspectionOverrideValidator(
        mutableListOf<InspectionOverrideRule>(
            InspectionOverrideNameLengthRule(3600),
            InspectionOverridePinnedGuardRule(64),
            InspectionOverrideAllowedCodeRule(mutableListOf<String>("default", "rolling", "tiered")),
            InspectionOverrideCodePrefixRule("X"),
            InspectionOverrideCodeLengthRule(255),
            InspectionOverrideAttemptsRangeRule(32, 255),
        ),
    )
}
