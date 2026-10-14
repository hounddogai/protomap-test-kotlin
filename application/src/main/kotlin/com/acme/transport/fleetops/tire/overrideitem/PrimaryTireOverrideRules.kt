package com.acme.transport.fleetops.tire.overrideitem

import com.acme.transport.kit.text.normalize

/** A rule a primary tire override must pass. Check returns its problems, or none. */
interface PrimaryTireOverrideRule {
    fun check(item: PrimaryTireOverride): MutableList<String>
}

data class PrimaryTireOverrideNameLengthRule(var maxLength: Int) : PrimaryTireOverrideRule {
    override fun check(item: PrimaryTireOverride): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryTireOverrideRequiredDriverIdRule(var message: String) : PrimaryTireOverrideRule {
    override fun check(item: PrimaryTireOverride): MutableList<String> {
        if (item.driverId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PrimaryTireOverrideFloorValueRangeRule(var min: Double, var max: Double) : PrimaryTireOverrideRule {
    override fun check(item: PrimaryTireOverride): MutableList<String> {
        val value = item.floorValue
        if (value < min || value > max) {
            return mutableListOf<String>("floor value $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryTireOverrideAllowedNameRule(var allowed: MutableList<String>) : PrimaryTireOverrideRule {
    override fun check(item: PrimaryTireOverride): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.name))) {
            return mutableListOf<String>("name ${item.name} is expired")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryTireOverrideMarginRangeRule(var min: Double, var max: Double) : PrimaryTireOverrideRule {
    override fun check(item: PrimaryTireOverride): MutableList<String> {
        val value = item.margin
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("margin $value is below $min")
        }
        if (value > max) {
            problems.add("margin $value is above $max")
        }
        return problems
    }
}

data class PrimaryTireOverrideCodePrefixRule(var prefix: String) : PrimaryTireOverrideRule {
    override fun check(item: PrimaryTireOverride): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a primary tire override. */
data class PrimaryTireOverrideValidator(var rules: MutableList<PrimaryTireOverrideRule>) {
    fun validate(item: PrimaryTireOverride): MutableList<String> {
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

    fun requireValid(item: PrimaryTireOverride) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PrimaryTireOverride>): MutableMap<String, MutableList<String>> {
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

fun defaultPrimaryTireOverrideValidator(): PrimaryTireOverrideValidator {
    return PrimaryTireOverrideValidator(
        mutableListOf<PrimaryTireOverrideRule>(
            PrimaryTireOverrideNameLengthRule(64),
            PrimaryTireOverrideRequiredDriverIdRule("primary tire override needs an entry"),
            PrimaryTireOverrideFloorValueRangeRule(1.7, 1144.6),
            PrimaryTireOverrideAllowedNameRule(mutableListOf<String>()),
            PrimaryTireOverrideMarginRangeRule(8.8, 4324.5),
            PrimaryTireOverrideCodePrefixRule("X"),
        ),
    )
}
