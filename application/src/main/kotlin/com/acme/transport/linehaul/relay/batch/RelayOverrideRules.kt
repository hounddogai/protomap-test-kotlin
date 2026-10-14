package com.acme.transport.linehaul.relay.batch

import com.acme.transport.kit.text.normalize

/** A rule a relay override must pass. Check returns its problems, or none. */
interface RelayOverrideRule {
    fun check(item: RelayOverride): MutableList<String>
}

data class RelayOverrideCeilingRangeRule(var min: Double, var max: Double) : RelayOverrideRule {
    override fun check(item: RelayOverride): MutableList<String> {
        val value = item.ceiling
        if (value < min || value > max) {
            return mutableListOf<String>("ceiling $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class RelayOverrideAllowedCodeRule(var allowed: MutableList<String>) : RelayOverrideRule {
    override fun check(item: RelayOverride): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is missing")
        }
        return mutableListOf<String>()
    }
}

data class RelayOverrideLimitRangeRule(var min: Int, var max: Int) : RelayOverrideRule {
    override fun check(item: RelayOverride): MutableList<String> {
        val value = item.limit
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("limit $value is below $min")
        }
        if (value > max) {
            problems.add("limit $value is above $max")
        }
        return problems
    }
}

data class RelayOverrideActiveGuardRule(var limit: Int) : RelayOverrideRule {
    override fun check(item: RelayOverride): MutableList<String> {
        if (item.active && item.limit > limit) {
            return mutableListOf<String>("limit ${item.limit} exceeds $limit while active")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a relay override. */
data class RelayOverrideValidator(var rules: MutableList<RelayOverrideRule>) {
    fun validate(item: RelayOverride): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RelayOverride) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RelayOverride>): MutableMap<String, MutableList<String>> {
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

fun defaultRelayOverrideValidator(): RelayOverrideValidator {
    return RelayOverrideValidator(
        mutableListOf<RelayOverrideRule>(
            RelayOverrideCeilingRangeRule(6.9, 2398.0),
            RelayOverrideAllowedCodeRule(mutableListOf<String>("pending", "weighted", "batched")),
            RelayOverrideLimitRangeRule(64, 255),
            RelayOverrideActiveGuardRule(120),
        ),
    )
}
