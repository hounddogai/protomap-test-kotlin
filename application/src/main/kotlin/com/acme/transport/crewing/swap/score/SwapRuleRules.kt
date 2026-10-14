package com.acme.transport.crewing.swap.score

import com.acme.transport.kit.text.normalize

/** A rule a swap rule must pass. Check returns its problems, or none. */
interface SwapRuleRule {
    fun check(item: SwapRule): MutableList<String>
}

data class SwapRuleArchivedGuardRule(var limit: Double) : SwapRuleRule {
    override fun check(item: SwapRule): MutableList<String> {
        if (item.archived && item.minimum > limit) {
            return mutableListOf<String>("minimum ${item.minimum} exceeds $limit while archived")
        }
        return mutableListOf<String>()
    }
}

data class SwapRuleStopCountRangeRule(var min: Int, var max: Int) : SwapRuleRule {
    override fun check(item: SwapRule): MutableList<String> {
        val value = item.stopCount
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("stop count $value is below $min")
        }
        if (value > max) {
            problems.add("stop count $value is above $max")
        }
        return problems
    }
}

data class SwapRuleAllowedDepotRule(var allowed: MutableList<String>) : SwapRuleRule {
    override fun check(item: SwapRule): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.depot))) {
            return mutableListOf<String>("depot ${item.depot} is duplicated")
        }
        return mutableListOf<String>()
    }
}

data class SwapRuleVehicleIdLengthRule(var maxLength: Int) : SwapRuleRule {
    override fun check(item: SwapRule): MutableList<String> {
        val size = item.vehicleId.length
        if (size > maxLength) {
            return mutableListOf<String>("vehicle id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a swap rule. */
data class SwapRuleValidator(var rules: MutableList<SwapRuleRule>) {
    fun validate(item: SwapRule): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: SwapRule) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<SwapRule>): MutableMap<String, MutableList<String>> {
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

fun defaultSwapRuleValidator(): SwapRuleValidator {
    return SwapRuleValidator(
        mutableListOf<SwapRuleRule>(
            SwapRuleArchivedGuardRule(8.3),
            SwapRuleStopCountRangeRule(32, 120),
            SwapRuleAllowedDepotRule(mutableListOf<String>()),
            SwapRuleVehicleIdLengthRule(120),
        ),
    )
}
