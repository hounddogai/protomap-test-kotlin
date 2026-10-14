package com.acme.transport.fleetops.tire.exception

import com.acme.transport.kit.text.normalize

/** A rule a legacy tire cycle must pass. Check returns its problems, or none. */
interface LegacyTireCycleRule {
    fun check(item: LegacyTireCycle): MutableList<String>
}

data class LegacyTireCycleAllowedShiftCodeRule(var allowed: MutableList<String>) : LegacyTireCycleRule {
    override fun check(item: LegacyTireCycle): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.shiftCode))) {
            return mutableListOf<String>("shift code ${item.shiftCode} is inconsistent")
        }
        return mutableListOf<String>()
    }
}

data class LegacyTireCycleTimezoneLengthRule(var maxLength: Int) : LegacyTireCycleRule {
    override fun check(item: LegacyTireCycle): MutableList<String> {
        val size = item.timezone.length
        if (size > maxLength) {
            return mutableListOf<String>("timezone is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LegacyTireCycleDockLengthRule(var maxLength: Int) : LegacyTireCycleRule {
    override fun check(item: LegacyTireCycle): MutableList<String> {
        val size = item.dock.length
        if (size > maxLength) {
            return mutableListOf<String>("dock is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LegacyTireCycleCodePrefixRule(var prefix: String) : LegacyTireCycleRule {
    override fun check(item: LegacyTireCycle): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class LegacyTireCycleRequiredTitleRule(var message: String) : LegacyTireCycleRule {
    override fun check(item: LegacyTireCycle): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LegacyTireCycleRequiredCodeRule(var message: String) : LegacyTireCycleRule {
    override fun check(item: LegacyTireCycle): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a legacy tire cycle. */
data class LegacyTireCycleValidator(var rules: MutableList<LegacyTireCycleRule>) {
    fun validate(item: LegacyTireCycle): MutableList<String> {
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

    fun requireValid(item: LegacyTireCycle) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LegacyTireCycle>): MutableMap<String, MutableList<String>> {
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

fun defaultLegacyTireCycleValidator(): LegacyTireCycleValidator {
    return LegacyTireCycleValidator(
        mutableListOf<LegacyTireCycleRule>(
            LegacyTireCycleAllowedShiftCodeRule(mutableListOf<String>("capped", "default", "global")),
            LegacyTireCycleTimezoneLengthRule(64),
            LegacyTireCycleDockLengthRule(120),
            LegacyTireCycleCodePrefixRule("Q"),
            LegacyTireCycleRequiredTitleRule("legacy tire cycle needs an entry"),
            LegacyTireCycleRequiredCodeRule("legacy tire cycle needs this field"),
        ),
    )
}
