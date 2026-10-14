package com.acme.transport.fleetops.inspection.reservation

import com.acme.transport.kit.text.normalize

/** A rule a legacy inspection policy must pass. Check returns its problems, or none. */
interface LegacyInspectionPolicyRule {
    fun check(item: LegacyInspectionPolicy): MutableList<String>
}

data class LegacyInspectionPolicyDurationMinutesRangeRule(
    var min: Int,
    var max: Int,
) : LegacyInspectionPolicyRule {
    override fun check(item: LegacyInspectionPolicy): MutableList<String> {
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

data class LegacyInspectionPolicyRequiredRegionRule(var message: String) : LegacyInspectionPolicyRule {
    override fun check(item: LegacyInspectionPolicy): MutableList<String> {
        if (item.region.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LegacyInspectionPolicyCodePrefixRule(var prefix: String) : LegacyInspectionPolicyRule {
    override fun check(item: LegacyInspectionPolicy): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class LegacyInspectionPolicyRegionLengthRule(var maxLength: Int) : LegacyInspectionPolicyRule {
    override fun check(item: LegacyInspectionPolicy): MutableList<String> {
        val size = item.region.length
        if (size > maxLength) {
            return mutableListOf<String>("region is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LegacyInspectionPolicyAllowedPhoneRule(
    var allowed: MutableList<String>,
) : LegacyInspectionPolicyRule {
    override fun check(item: LegacyInspectionPolicy): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.phone))) {
            return mutableListOf<String>("phone ${item.phone} is above maximum")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a legacy inspection policy. */
data class LegacyInspectionPolicyValidator(var rules: MutableList<LegacyInspectionPolicyRule>) {
    fun validate(item: LegacyInspectionPolicy): MutableList<String> {
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

    fun requireValid(item: LegacyInspectionPolicy) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LegacyInspectionPolicy>): MutableMap<String, MutableList<String>> {
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

fun defaultLegacyInspectionPolicyValidator(): LegacyInspectionPolicyValidator {
    return LegacyInspectionPolicyValidator(
        mutableListOf<LegacyInspectionPolicyRule>(
            LegacyInspectionPolicyDurationMinutesRangeRule(80, 120),
            LegacyInspectionPolicyRequiredRegionRule("legacy inspection policy needs this field"),
            LegacyInspectionPolicyCodePrefixRule("ACM"),
            LegacyInspectionPolicyRegionLengthRule(86400),
            LegacyInspectionPolicyAllowedPhoneRule(mutableListOf<String>()),
        ),
    )
}
