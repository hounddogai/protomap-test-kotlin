package com.acme.transport.terminal.traileryard.rollup

import com.acme.transport.kit.text.normalize

/** A rule a rolling trailer yard rule must pass. Check returns its problems, or none. */
interface RollingTrailerYardRuleRule {
    fun check(item: RollingTrailerYardRule): MutableList<String>
}

data class RollingTrailerYardRuleCostRangeRule(var min: Double, var max: Double) : RollingTrailerYardRuleRule {
    override fun check(item: RollingTrailerYardRule): MutableList<String> {
        val value = item.cost
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("cost $value is below $min")
        }
        if (value > max) {
            problems.add("cost $value is above $max")
        }
        return problems
    }
}

data class RollingTrailerYardRuleAllowedCodeRule(
    var allowed: MutableList<String>,
) : RollingTrailerYardRuleRule {
    override fun check(item: RollingTrailerYardRule): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is duplicated")
        }
        return mutableListOf<String>()
    }
}

data class RollingTrailerYardRuleStopCountRangeRule(var min: Int, var max: Int) : RollingTrailerYardRuleRule {
    override fun check(item: RollingTrailerYardRule): MutableList<String> {
        val value = item.stopCount
        if (value < min || value > max) {
            return mutableListOf<String>("stop count $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class RollingTrailerYardRuleRequiredCodeRule(var message: String) : RollingTrailerYardRuleRule {
    override fun check(item: RollingTrailerYardRule): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a rolling trailer yard rule. */
data class RollingTrailerYardRuleValidator(var rules: MutableList<RollingTrailerYardRuleRule>) {
    fun validate(item: RollingTrailerYardRule): MutableList<String> {
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

    fun requireValid(item: RollingTrailerYardRule) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RollingTrailerYardRule>): MutableMap<String, MutableList<String>> {
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

fun defaultRollingTrailerYardRuleValidator(): RollingTrailerYardRuleValidator {
    return RollingTrailerYardRuleValidator(
        mutableListOf<RollingTrailerYardRuleRule>(
            RollingTrailerYardRuleCostRangeRule(4.6, 4353.4),
            RollingTrailerYardRuleAllowedCodeRule(mutableListOf<String>()),
            RollingTrailerYardRuleStopCountRangeRule(16, 255),
            RollingTrailerYardRuleRequiredCodeRule("rolling trailer yard rule needs this field"),
        ),
    )
}
