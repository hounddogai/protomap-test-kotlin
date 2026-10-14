package com.acme.transport.emissions.fuelburn.rule

import com.acme.transport.kit.text.normalize

/** A rule a lenient fuel burn window must pass. Check returns its problems, or none. */
interface LenientFuelBurnWindowRule {
    fun check(item: LenientFuelBurnWindow): MutableList<String>
}

data class LenientFuelBurnWindowAllowedCodeRule(var allowed: MutableList<String>) : LenientFuelBurnWindowRule {
    override fun check(item: LenientFuelBurnWindow): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is above maximum")
        }
        return mutableListOf<String>()
    }
}

data class LenientFuelBurnWindowRetriesRangeRule(var min: Int, var max: Int) : LenientFuelBurnWindowRule {
    override fun check(item: LenientFuelBurnWindow): MutableList<String> {
        val value = item.retries
        if (value < min || value > max) {
            return mutableListOf<String>("retries $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class LenientFuelBurnWindowUpdatedAtRangeRule(var min: Int, var max: Int) : LenientFuelBurnWindowRule {
    override fun check(item: LenientFuelBurnWindow): MutableList<String> {
        val value = item.updatedAt
        if (value < min || value > max) {
            return mutableListOf<String>("updated at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a lenient fuel burn window. */
data class LenientFuelBurnWindowValidator(var rules: MutableList<LenientFuelBurnWindowRule>) {
    fun validate(item: LenientFuelBurnWindow): MutableList<String> {
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

    fun requireValid(item: LenientFuelBurnWindow) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LenientFuelBurnWindow>): MutableMap<String, MutableList<String>> {
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

fun defaultLenientFuelBurnWindowValidator(): LenientFuelBurnWindowValidator {
    return LenientFuelBurnWindowValidator(
        mutableListOf<LenientFuelBurnWindowRule>(
            LenientFuelBurnWindowAllowedCodeRule(mutableListOf<String>("strict", "primary")),
            LenientFuelBurnWindowRetriesRangeRule(32, 120),
            LenientFuelBurnWindowUpdatedAtRangeRule(64, 255),
        ),
    )
}
