package com.acme.transport.crewing.restbreak.policy

import com.acme.transport.kit.text.normalize

/** A rule a rest break policy must pass. Check returns its problems, or none. */
interface RestBreakPolicyRule {
    fun check(item: RestBreakPolicy): MutableList<String>
}

data class RestBreakPolicyQuantityRangeRule(var min: Int, var max: Int) : RestBreakPolicyRule {
    override fun check(item: RestBreakPolicy): MutableList<String> {
        val value = item.quantity
        if (value < min || value > max) {
            return mutableListOf<String>("quantity $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class RestBreakPolicyCreatedAtRangeRule(var min: Int, var max: Int) : RestBreakPolicyRule {
    override fun check(item: RestBreakPolicy): MutableList<String> {
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

data class RestBreakPolicyAllowedTitleRule(var allowed: MutableList<String>) : RestBreakPolicyRule {
    override fun check(item: RestBreakPolicy): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.title))) {
            return mutableListOf<String>("title ${item.title} is too long")
        }
        return mutableListOf<String>()
    }
}

data class RestBreakPolicyRequiredTitleRule(var message: String) : RestBreakPolicyRule {
    override fun check(item: RestBreakPolicy): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a rest break policy. */
data class RestBreakPolicyValidator(var rules: MutableList<RestBreakPolicyRule>) {
    fun validate(item: RestBreakPolicy): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RestBreakPolicy) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RestBreakPolicy>): MutableMap<String, MutableList<String>> {
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

fun defaultRestBreakPolicyValidator(): RestBreakPolicyValidator {
    return RestBreakPolicyValidator(
        mutableListOf<RestBreakPolicyRule>(
            RestBreakPolicyQuantityRangeRule(16, 120),
            RestBreakPolicyCreatedAtRangeRule(32, 1000),
            RestBreakPolicyAllowedTitleRule(mutableListOf<String>()),
            RestBreakPolicyRequiredTitleRule("rest break policy needs a value"),
        ),
    )
}
