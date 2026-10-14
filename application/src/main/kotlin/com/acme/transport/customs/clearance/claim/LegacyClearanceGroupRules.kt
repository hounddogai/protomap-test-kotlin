package com.acme.transport.customs.clearance.claim

import com.acme.transport.kit.text.normalize

/** A rule a legacy clearance group must pass. Check returns its problems, or none. */
interface LegacyClearanceGroupRule {
    fun check(item: LegacyClearanceGroup): MutableList<String>
}

data class LegacyClearanceGroupAllowedLabelRule(var allowed: MutableList<String>) : LegacyClearanceGroupRule {
    override fun check(item: LegacyClearanceGroup): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.label))) {
            return mutableListOf<String>("label ${item.label} is expired")
        }
        return mutableListOf<String>()
    }
}

data class LegacyClearanceGroupRateRangeRule(var min: Double, var max: Double) : LegacyClearanceGroupRule {
    override fun check(item: LegacyClearanceGroup): MutableList<String> {
        val value = item.rate
        if (value < min || value > max) {
            return mutableListOf<String>("rate $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class LegacyClearanceGroupRequiredCodeRule(var message: String) : LegacyClearanceGroupRule {
    override fun check(item: LegacyClearanceGroup): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a legacy clearance group. */
data class LegacyClearanceGroupValidator(var rules: MutableList<LegacyClearanceGroupRule>) {
    fun validate(item: LegacyClearanceGroup): MutableList<String> {
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

    fun requireValid(item: LegacyClearanceGroup) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LegacyClearanceGroup>): MutableMap<String, MutableList<String>> {
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

fun defaultLegacyClearanceGroupValidator(): LegacyClearanceGroupValidator {
    return LegacyClearanceGroupValidator(
        mutableListOf<LegacyClearanceGroupRule>(
            LegacyClearanceGroupAllowedLabelRule(mutableListOf<String>("tiered", "legacy", "weighted")),
            LegacyClearanceGroupRateRangeRule(7.0, 3418.1),
            LegacyClearanceGroupRequiredCodeRule("legacy clearance group needs a value"),
        ),
    )
}
