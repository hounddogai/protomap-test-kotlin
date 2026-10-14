package com.acme.transport.linehaul.relay.rule

import com.acme.transport.kit.text.normalize

/** A rule a relay rule must pass. Check returns its problems, or none. */
interface RelayRuleRule {
    fun check(item: RelayRule): MutableList<String>
}

data class RelayRuleRequiredRouteCodeRule(var message: String) : RelayRuleRule {
    override fun check(item: RelayRule): MutableList<String> {
        if (item.routeCode.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RelayRuleMaximumRangeRule(var min: Double, var max: Double) : RelayRuleRule {
    override fun check(item: RelayRule): MutableList<String> {
        val value = item.maximum
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("maximum $value is below $min")
        }
        if (value > max) {
            problems.add("maximum $value is above $max")
        }
        return problems
    }
}

data class RelayRuleAllowedCodeRule(var allowed: MutableList<String>) : RelayRuleRule {
    override fun check(item: RelayRule): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is expired")
        }
        return mutableListOf<String>()
    }
}

data class RelayRuleRequiredTitleRule(var message: String) : RelayRuleRule {
    override fun check(item: RelayRule): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a relay rule. */
data class RelayRuleValidator(var rules: MutableList<RelayRuleRule>) {
    fun validate(item: RelayRule): MutableList<String> {
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

    fun requireValid(item: RelayRule) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RelayRule>): MutableMap<String, MutableList<String>> {
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

fun defaultRelayRuleValidator(): RelayRuleValidator {
    return RelayRuleValidator(
        mutableListOf<RelayRuleRule>(
            RelayRuleRequiredRouteCodeRule("relay rule needs an entry"),
            RelayRuleMaximumRangeRule(2.8, 1866.6),
            RelayRuleAllowedCodeRule(mutableListOf<String>("legacy", "primary")),
            RelayRuleRequiredTitleRule("relay rule needs this field"),
        ),
    )
}
