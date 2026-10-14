package com.acme.transport.freightpay.dispute.disputebundle

import com.acme.transport.kit.text.normalize

/** A rule a dispute rule must pass. Check returns its problems, or none. */
interface DisputeRuleRule {
    fun check(item: DisputeRule): MutableList<String>
}

data class DisputeRuleAllowedDriverIdRule(var allowed: MutableList<String>) : DisputeRuleRule {
    override fun check(item: DisputeRule): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.driverId))) {
            return mutableListOf<String>("driver id ${item.driverId} is duplicated")
        }
        return mutableListOf<String>()
    }
}

data class DisputeRuleRequiredTitleRule(var message: String) : DisputeRuleRule {
    override fun check(item: DisputeRule): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DisputeRuleRatioRangeRule(var min: Double, var max: Double) : DisputeRuleRule {
    override fun check(item: DisputeRule): MutableList<String> {
        val value = item.ratio
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("ratio $value is below $min")
        }
        if (value > max) {
            problems.add("ratio $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a dispute rule. */
data class DisputeRuleValidator(var rules: MutableList<DisputeRuleRule>) {
    fun validate(item: DisputeRule): MutableList<String> {
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

    fun requireValid(item: DisputeRule) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DisputeRule>): MutableMap<String, MutableList<String>> {
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

fun defaultDisputeRuleValidator(): DisputeRuleValidator {
    return DisputeRuleValidator(
        mutableListOf<DisputeRuleRule>(
            DisputeRuleAllowedDriverIdRule(mutableListOf<String>("fallback")),
            DisputeRuleRequiredTitleRule("dispute rule needs an entry"),
            DisputeRuleRatioRangeRule(7.0, 2435.9),
        ),
    )
}
