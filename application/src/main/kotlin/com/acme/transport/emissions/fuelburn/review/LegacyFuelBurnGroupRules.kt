package com.acme.transport.emissions.fuelburn.review

import com.acme.transport.kit.text.normalize

/** A rule a legacy fuel burn group must pass. Check returns its problems, or none. */
interface LegacyFuelBurnGroupRule {
    fun check(item: LegacyFuelBurnGroup): MutableList<String>
}

data class LegacyFuelBurnGroupExpiresAtRangeRule(var min: Int, var max: Int) : LegacyFuelBurnGroupRule {
    override fun check(item: LegacyFuelBurnGroup): MutableList<String> {
        val value = item.expiresAt
        if (value < min || value > max) {
            return mutableListOf<String>("expires at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class LegacyFuelBurnGroupThresholdRangeRule(var min: Double, var max: Double) : LegacyFuelBurnGroupRule {
    override fun check(item: LegacyFuelBurnGroup): MutableList<String> {
        val value = item.threshold
        if (value < min || value > max) {
            return mutableListOf<String>("threshold $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class LegacyFuelBurnGroupAllowedCodeRule(var allowed: MutableList<String>) : LegacyFuelBurnGroupRule {
    override fun check(item: LegacyFuelBurnGroup): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is blocked")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a legacy fuel burn group. */
data class LegacyFuelBurnGroupValidator(var rules: MutableList<LegacyFuelBurnGroupRule>) {
    fun validate(item: LegacyFuelBurnGroup): MutableList<String> {
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

    fun requireValid(item: LegacyFuelBurnGroup) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LegacyFuelBurnGroup>): MutableMap<String, MutableList<String>> {
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

fun defaultLegacyFuelBurnGroupValidator(): LegacyFuelBurnGroupValidator {
    return LegacyFuelBurnGroupValidator(
        mutableListOf<LegacyFuelBurnGroupRule>(
            LegacyFuelBurnGroupExpiresAtRangeRule(255, 86400),
            LegacyFuelBurnGroupThresholdRangeRule(5.7, 119.3),
            LegacyFuelBurnGroupAllowedCodeRule(mutableListOf<String>("lenient")),
        ),
    )
}
