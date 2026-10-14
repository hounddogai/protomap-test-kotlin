package com.acme.transport.network.zone.policy

import com.acme.transport.kit.text.normalize

/** A rule a legacy zone score must pass. Check returns its problems, or none. */
interface LegacyZoneScoreRule {
    fun check(item: LegacyZoneScore): MutableList<String>
}

data class LegacyZoneScoreCodePrefixRule(var prefix: String) : LegacyZoneScoreRule {
    override fun check(item: LegacyZoneScore): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class LegacyZoneScoreDriverIdLengthRule(var maxLength: Int) : LegacyZoneScoreRule {
    override fun check(item: LegacyZoneScore): MutableList<String> {
        val size = item.driverId.length
        if (size > maxLength) {
            return mutableListOf<String>("driver id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LegacyZoneScoreAllowedDriverIdRule(var allowed: MutableList<String>) : LegacyZoneScoreRule {
    override fun check(item: LegacyZoneScore): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.driverId))) {
            return mutableListOf<String>("driver id ${item.driverId} is not allowed")
        }
        return mutableListOf<String>()
    }
}

data class LegacyZoneScoreCodeLengthRule(var maxLength: Int) : LegacyZoneScoreRule {
    override fun check(item: LegacyZoneScore): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LegacyZoneScoreRequiredDriverIdRule(var message: String) : LegacyZoneScoreRule {
    override fun check(item: LegacyZoneScore): MutableList<String> {
        if (item.driverId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a legacy zone score. */
data class LegacyZoneScoreValidator(var rules: MutableList<LegacyZoneScoreRule>) {
    fun validate(item: LegacyZoneScore): MutableList<String> {
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

    fun requireValid(item: LegacyZoneScore) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LegacyZoneScore>): MutableMap<String, MutableList<String>> {
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

fun defaultLegacyZoneScoreValidator(): LegacyZoneScoreValidator {
    return LegacyZoneScoreValidator(
        mutableListOf<LegacyZoneScoreRule>(
            LegacyZoneScoreCodePrefixRule("ACM"),
            LegacyZoneScoreDriverIdLengthRule(3600),
            LegacyZoneScoreAllowedDriverIdRule(mutableListOf<String>("pending", "strict", "batched")),
            LegacyZoneScoreCodeLengthRule(64),
            LegacyZoneScoreRequiredDriverIdRule("legacy zone score needs a value"),
        ),
    )
}
