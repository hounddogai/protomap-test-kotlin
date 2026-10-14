package com.acme.transport.network.hub.milestone

import com.acme.transport.kit.text.normalize

/** A rule a legacy hub slot must pass. Check returns its problems, or none. */
interface LegacyHubSlotRule {
    fun check(item: LegacyHubSlot): MutableList<String>
}

data class LegacyHubSlotAllowedLabelRule(var allowed: MutableList<String>) : LegacyHubSlotRule {
    override fun check(item: LegacyHubSlot): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.label))) {
            return mutableListOf<String>("label ${item.label} is not allowed")
        }
        return mutableListOf<String>()
    }
}

data class LegacyHubSlotDistanceKmRangeRule(var min: Double, var max: Double) : LegacyHubSlotRule {
    override fun check(item: LegacyHubSlot): MutableList<String> {
        val value = item.distanceKm
        if (value < min || value > max) {
            return mutableListOf<String>("distance km $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class LegacyHubSlotUpdatedAtRangeRule(var min: Int, var max: Int) : LegacyHubSlotRule {
    override fun check(item: LegacyHubSlot): MutableList<String> {
        val value = item.updatedAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("updated at $value is below $min")
        }
        if (value > max) {
            problems.add("updated at $value is above $max")
        }
        return problems
    }
}

data class LegacyHubSlotCountryLengthRule(var maxLength: Int) : LegacyHubSlotRule {
    override fun check(item: LegacyHubSlot): MutableList<String> {
        val size = item.country.length
        if (size > maxLength) {
            return mutableListOf<String>("country is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LegacyHubSlotCodePrefixRule(var prefix: String) : LegacyHubSlotRule {
    override fun check(item: LegacyHubSlot): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class LegacyHubSlotRequiredLabelRule(var message: String) : LegacyHubSlotRule {
    override fun check(item: LegacyHubSlot): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a legacy hub slot. */
data class LegacyHubSlotValidator(var rules: MutableList<LegacyHubSlotRule>) {
    fun validate(item: LegacyHubSlot): MutableList<String> {
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

    fun requireValid(item: LegacyHubSlot) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LegacyHubSlot>): MutableMap<String, MutableList<String>> {
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

fun defaultLegacyHubSlotValidator(): LegacyHubSlotValidator {
    return LegacyHubSlotValidator(
        mutableListOf<LegacyHubSlotRule>(
            LegacyHubSlotAllowedLabelRule(mutableListOf<String>("batched", "fallback", "weighted")),
            LegacyHubSlotDistanceKmRangeRule(2.0, 2994.4),
            LegacyHubSlotUpdatedAtRangeRule(16, 86400),
            LegacyHubSlotCountryLengthRule(64),
            LegacyHubSlotCodePrefixRule("OPS"),
            LegacyHubSlotRequiredLabelRule("legacy hub slot needs an entry"),
        ),
    )
}
