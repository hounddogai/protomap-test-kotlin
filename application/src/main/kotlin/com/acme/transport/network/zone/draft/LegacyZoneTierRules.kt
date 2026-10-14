package com.acme.transport.network.zone.draft

/** A rule a legacy zone tier must pass. Check returns its problems, or none. */
interface LegacyZoneTierRule {
    fun check(item: LegacyZoneTier): MutableList<String>
}

data class LegacyZoneTierDepotLengthRule(var maxLength: Int) : LegacyZoneTierRule {
    override fun check(item: LegacyZoneTier): MutableList<String> {
        val size = item.depot.length
        if (size > maxLength) {
            return mutableListOf<String>("depot is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LegacyZoneTierRetriesRangeRule(var min: Int, var max: Int) : LegacyZoneTierRule {
    override fun check(item: LegacyZoneTier): MutableList<String> {
        val value = item.retries
        if (value < min || value > max) {
            return mutableListOf<String>("retries $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class LegacyZoneTierCodeLengthRule(var maxLength: Int) : LegacyZoneTierRule {
    override fun check(item: LegacyZoneTier): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a legacy zone tier. */
data class LegacyZoneTierValidator(var rules: MutableList<LegacyZoneTierRule>) {
    fun validate(item: LegacyZoneTier): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: LegacyZoneTier) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LegacyZoneTier>): MutableMap<String, MutableList<String>> {
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

fun defaultLegacyZoneTierValidator(): LegacyZoneTierValidator {
    return LegacyZoneTierValidator(
        mutableListOf<LegacyZoneTierRule>(
            LegacyZoneTierDepotLengthRule(3600),
            LegacyZoneTierRetriesRangeRule(64, 86400),
            LegacyZoneTierCodeLengthRule(3600),
        ),
    )
}
