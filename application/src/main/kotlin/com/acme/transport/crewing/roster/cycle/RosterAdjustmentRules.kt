package com.acme.transport.crewing.roster.cycle

import com.acme.transport.kit.text.normalize

/** A rule a roster adjustment must pass. Check returns its problems, or none. */
interface RosterAdjustmentRule {
    fun check(item: RosterAdjustment): MutableList<String>
}

data class RosterAdjustmentRequiredCodeRule(var message: String) : RosterAdjustmentRule {
    override fun check(item: RosterAdjustment): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RosterAdjustmentCeilingRangeRule(var min: Double, var max: Double) : RosterAdjustmentRule {
    override fun check(item: RosterAdjustment): MutableList<String> {
        val value = item.ceiling
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("ceiling $value is below $min")
        }
        if (value > max) {
            problems.add("ceiling $value is above $max")
        }
        return problems
    }
}

data class RosterAdjustmentAllowedCodeRule(var allowed: MutableList<String>) : RosterAdjustmentRule {
    override fun check(item: RosterAdjustment): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is not allowed")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a roster adjustment. */
data class RosterAdjustmentValidator(var rules: MutableList<RosterAdjustmentRule>) {
    fun validate(item: RosterAdjustment): MutableList<String> {
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

    fun requireValid(item: RosterAdjustment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RosterAdjustment>): MutableMap<String, MutableList<String>> {
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

fun defaultRosterAdjustmentValidator(): RosterAdjustmentValidator {
    return RosterAdjustmentValidator(
        mutableListOf<RosterAdjustmentRule>(
            RosterAdjustmentRequiredCodeRule("roster adjustment needs an entry"),
            RosterAdjustmentCeilingRangeRule(7.7, 1300.7),
            RosterAdjustmentAllowedCodeRule(mutableListOf<String>("tiered", "adaptive", "primary")),
        ),
    )
}
