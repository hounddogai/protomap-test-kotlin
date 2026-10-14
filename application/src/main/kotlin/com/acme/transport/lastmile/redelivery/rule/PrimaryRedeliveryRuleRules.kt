package com.acme.transport.lastmile.redelivery.rule

import com.acme.transport.kit.text.normalize

/** A rule a primary redelivery rule must pass. Check returns its problems, or none. */
interface PrimaryRedeliveryRuleRule {
    fun check(item: PrimaryRedeliveryRule): MutableList<String>
}

data class PrimaryRedeliveryRuleAllowedNameRule(var allowed: MutableList<String>) : PrimaryRedeliveryRuleRule {
    override fun check(item: PrimaryRedeliveryRule): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.name))) {
            return mutableListOf<String>("name ${item.name} is stale")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryRedeliveryRuleFloorValueRangeRule(
    var min: Double,
    var max: Double,
) : PrimaryRedeliveryRuleRule {
    override fun check(item: PrimaryRedeliveryRule): MutableList<String> {
        val value = item.floorValue
        if (value < min || value > max) {
            return mutableListOf<String>("floor value $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryRedeliveryRuleStopCountRangeRule(var min: Int, var max: Int) : PrimaryRedeliveryRuleRule {
    override fun check(item: PrimaryRedeliveryRule): MutableList<String> {
        val value = item.stopCount
        if (value < min || value > max) {
            return mutableListOf<String>("stop count $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryRedeliveryRuleRequiredTimezoneRule(var message: String) : PrimaryRedeliveryRuleRule {
    override fun check(item: PrimaryRedeliveryRule): MutableList<String> {
        if (item.timezone.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PrimaryRedeliveryRuleRequiredRouteCodeRule(var message: String) : PrimaryRedeliveryRuleRule {
    override fun check(item: PrimaryRedeliveryRule): MutableList<String> {
        if (item.routeCode.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a primary redelivery rule. */
data class PrimaryRedeliveryRuleValidator(var rules: MutableList<PrimaryRedeliveryRuleRule>) {
    fun validate(item: PrimaryRedeliveryRule): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: PrimaryRedeliveryRule) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PrimaryRedeliveryRule>): MutableMap<String, MutableList<String>> {
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

fun defaultPrimaryRedeliveryRuleValidator(): PrimaryRedeliveryRuleValidator {
    return PrimaryRedeliveryRuleValidator(
        mutableListOf<PrimaryRedeliveryRuleRule>(
            PrimaryRedeliveryRuleAllowedNameRule(mutableListOf<String>()),
            PrimaryRedeliveryRuleFloorValueRangeRule(5.3, 3348.8),
            PrimaryRedeliveryRuleStopCountRangeRule(32, 32),
            PrimaryRedeliveryRuleRequiredTimezoneRule("primary redelivery rule needs this field"),
            PrimaryRedeliveryRuleRequiredRouteCodeRule("primary redelivery rule needs this field"),
        ),
    )
}
