package com.acme.transport.crewing.roster.exception

import com.acme.transport.kit.text.normalize

/** A rule a roster policy must pass. Check returns its problems, or none. */
interface RosterPolicyRule {
    fun check(item: RosterPolicy): MutableList<String>
}

data class RosterPolicyRequiredLabelRule(var message: String) : RosterPolicyRule {
    override fun check(item: RosterPolicy): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RosterPolicyRetriesRangeRule(var min: Int, var max: Int) : RosterPolicyRule {
    override fun check(item: RosterPolicy): MutableList<String> {
        val value = item.retries
        if (value < min || value > max) {
            return mutableListOf<String>("retries $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class RosterPolicyAllowedLabelRule(var allowed: MutableList<String>) : RosterPolicyRule {
    override fun check(item: RosterPolicy): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.label))) {
            return mutableListOf<String>("label ${item.label} is duplicated")
        }
        return mutableListOf<String>()
    }
}

data class RosterPolicyCodePrefixRule(var prefix: String) : RosterPolicyRule {
    override fun check(item: RosterPolicy): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class RosterPolicyLabelLengthRule(var maxLength: Int) : RosterPolicyRule {
    override fun check(item: RosterPolicy): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a roster policy. */
data class RosterPolicyValidator(var rules: MutableList<RosterPolicyRule>) {
    fun validate(item: RosterPolicy): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RosterPolicy) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RosterPolicy>): MutableMap<String, MutableList<String>> {
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

fun defaultRosterPolicyValidator(): RosterPolicyValidator {
    return RosterPolicyValidator(
        mutableListOf<RosterPolicyRule>(
            RosterPolicyRequiredLabelRule("roster policy needs an entry"),
            RosterPolicyRetriesRangeRule(16, 1000),
            RosterPolicyAllowedLabelRule(mutableListOf<String>("tiered", "rolling", "scheduled")),
            RosterPolicyCodePrefixRule("X"),
            RosterPolicyLabelLengthRule(120),
        ),
    )
}
