package com.acme.transport.network.zone.overrideitem

import com.acme.transport.kit.text.normalize

/** A rule a legacy zone override must pass. Check returns its problems, or none. */
interface LegacyZoneOverrideRule {
    fun check(item: LegacyZoneOverride): MutableList<String>
}

data class LegacyZoneOverrideRequiredTitleRule(var message: String) : LegacyZoneOverrideRule {
    override fun check(item: LegacyZoneOverride): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LegacyZoneOverrideShiftCodeLengthRule(var maxLength: Int) : LegacyZoneOverrideRule {
    override fun check(item: LegacyZoneOverride): MutableList<String> {
        val size = item.shiftCode.length
        if (size > maxLength) {
            return mutableListOf<String>("shift code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LegacyZoneOverrideAllowedOwnerIdRule(var allowed: MutableList<String>) : LegacyZoneOverrideRule {
    override fun check(item: LegacyZoneOverride): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.ownerId))) {
            return mutableListOf<String>("owner id ${item.ownerId} is below minimum")
        }
        return mutableListOf<String>()
    }
}

data class LegacyZoneOverrideRatioRangeRule(var min: Double, var max: Double) : LegacyZoneOverrideRule {
    override fun check(item: LegacyZoneOverride): MutableList<String> {
        val value = item.ratio
        if (value < min || value > max) {
            return mutableListOf<String>("ratio $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a legacy zone override. */
data class LegacyZoneOverrideValidator(var rules: MutableList<LegacyZoneOverrideRule>) {
    fun validate(item: LegacyZoneOverride): MutableList<String> {
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

    fun requireValid(item: LegacyZoneOverride) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LegacyZoneOverride>): MutableMap<String, MutableList<String>> {
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

fun defaultLegacyZoneOverrideValidator(): LegacyZoneOverrideValidator {
    return LegacyZoneOverrideValidator(
        mutableListOf<LegacyZoneOverrideRule>(
            LegacyZoneOverrideRequiredTitleRule("legacy zone override needs a value"),
            LegacyZoneOverrideShiftCodeLengthRule(3600),
            LegacyZoneOverrideAllowedOwnerIdRule(mutableListOf<String>("rolling", "pending")),
            LegacyZoneOverrideRatioRangeRule(3.3, 361.0),
        ),
    )
}
