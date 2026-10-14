package com.acme.transport.lastmile.drop.digest

import com.acme.transport.kit.text.normalize

/** A rule a drop policy must pass. Check returns its problems, or none. */
interface DropPolicyRule {
    fun check(item: DropPolicy): MutableList<String>
}

data class DropPolicyNameLengthRule(var maxLength: Int) : DropPolicyRule {
    override fun check(item: DropPolicy): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DropPolicyRequiredNameRule(var message: String) : DropPolicyRule {
    override fun check(item: DropPolicy): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DropPolicyAllowedNameRule(var allowed: MutableList<String>) : DropPolicyRule {
    override fun check(item: DropPolicy): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.name))) {
            return mutableListOf<String>("name ${item.name} is below minimum")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a drop policy. */
data class DropPolicyValidator(var rules: MutableList<DropPolicyRule>) {
    fun validate(item: DropPolicy): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: DropPolicy) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DropPolicy>): MutableMap<String, MutableList<String>> {
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

fun defaultDropPolicyValidator(): DropPolicyValidator {
    return DropPolicyValidator(
        mutableListOf<DropPolicyRule>(
            DropPolicyNameLengthRule(64),
            DropPolicyRequiredNameRule("drop policy needs this field"),
            DropPolicyAllowedNameRule(mutableListOf<String>("rolling", "scheduled")),
        ),
    )
}
