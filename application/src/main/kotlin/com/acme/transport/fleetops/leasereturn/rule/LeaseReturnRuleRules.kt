package com.acme.transport.fleetops.leasereturn.rule

import com.acme.transport.kit.text.normalize

/** A rule a lease return rule must pass. Check returns its problems, or none. */
interface LeaseReturnRuleRule {
    fun check(item: LeaseReturnRule): MutableList<String>
}

data class LeaseReturnRuleCodeLengthRule(var maxLength: Int) : LeaseReturnRuleRule {
    override fun check(item: LeaseReturnRule): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LeaseReturnRuleCreatedAtRangeRule(var min: Int, var max: Int) : LeaseReturnRuleRule {
    override fun check(item: LeaseReturnRule): MutableList<String> {
        val value = item.createdAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("created at $value is below $min")
        }
        if (value > max) {
            problems.add("created at $value is above $max")
        }
        return problems
    }
}

data class LeaseReturnRuleAllowedNameRule(var allowed: MutableList<String>) : LeaseReturnRuleRule {
    override fun check(item: LeaseReturnRule): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.name))) {
            return mutableListOf<String>("name ${item.name} is too long")
        }
        return mutableListOf<String>()
    }
}

data class LeaseReturnRuleNameLengthRule(var maxLength: Int) : LeaseReturnRuleRule {
    override fun check(item: LeaseReturnRule): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LeaseReturnRuleRequiredNameRule(var message: String) : LeaseReturnRuleRule {
    override fun check(item: LeaseReturnRule): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LeaseReturnRuleRequiredUnitRule(var message: String) : LeaseReturnRuleRule {
    override fun check(item: LeaseReturnRule): MutableList<String> {
        if (item.unit.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a lease return rule. */
data class LeaseReturnRuleValidator(var rules: MutableList<LeaseReturnRuleRule>) {
    fun validate(item: LeaseReturnRule): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: LeaseReturnRule) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LeaseReturnRule>): MutableMap<String, MutableList<String>> {
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

fun defaultLeaseReturnRuleValidator(): LeaseReturnRuleValidator {
    return LeaseReturnRuleValidator(
        mutableListOf<LeaseReturnRuleRule>(
            LeaseReturnRuleCodeLengthRule(86400),
            LeaseReturnRuleCreatedAtRangeRule(255, 1000),
            LeaseReturnRuleAllowedNameRule(mutableListOf<String>("lenient", "adaptive", "manual")),
            LeaseReturnRuleNameLengthRule(16),
            LeaseReturnRuleRequiredNameRule("lease return rule needs a value"),
            LeaseReturnRuleRequiredUnitRule("lease return rule needs a value"),
        ),
    )
}
