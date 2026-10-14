package com.acme.transport.safety.nearmiss.misscycle

import com.acme.transport.kit.text.normalize

/** A rule a strict near miss policy must pass. Check returns its problems, or none. */
interface StrictNearMissPolicyRule {
    fun check(item: StrictNearMissPolicy): MutableList<String>
}

data class StrictNearMissPolicyRevisionRangeRule(var min: Int, var max: Int) : StrictNearMissPolicyRule {
    override fun check(item: StrictNearMissPolicy): MutableList<String> {
        val value = item.revision
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("revision $value is below $min")
        }
        if (value > max) {
            problems.add("revision $value is above $max")
        }
        return problems
    }
}

data class StrictNearMissPolicyCodePrefixRule(var prefix: String) : StrictNearMissPolicyRule {
    override fun check(item: StrictNearMissPolicy): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class StrictNearMissPolicyAllowedCodeRule(var allowed: MutableList<String>) : StrictNearMissPolicyRule {
    override fun check(item: StrictNearMissPolicy): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is duplicated")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a strict near miss policy. */
data class StrictNearMissPolicyValidator(var rules: MutableList<StrictNearMissPolicyRule>) {
    fun validate(item: StrictNearMissPolicy): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: StrictNearMissPolicy) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<StrictNearMissPolicy>): MutableMap<String, MutableList<String>> {
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

fun defaultStrictNearMissPolicyValidator(): StrictNearMissPolicyValidator {
    return StrictNearMissPolicyValidator(
        mutableListOf<StrictNearMissPolicyRule>(
            StrictNearMissPolicyRevisionRangeRule(16, 1000),
            StrictNearMissPolicyCodePrefixRule("Q"),
            StrictNearMissPolicyAllowedCodeRule(mutableListOf<String>()),
        ),
    )
}
