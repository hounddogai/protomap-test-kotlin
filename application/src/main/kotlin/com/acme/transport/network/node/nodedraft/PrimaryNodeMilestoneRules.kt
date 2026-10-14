package com.acme.transport.network.node.nodedraft

import com.acme.transport.kit.text.normalize

/** A rule a primary node milestone must pass. Check returns its problems, or none. */
interface PrimaryNodeMilestoneRule {
    fun check(item: PrimaryNodeMilestone): MutableList<String>
}

data class PrimaryNodeMilestoneNameLengthRule(var maxLength: Int) : PrimaryNodeMilestoneRule {
    override fun check(item: PrimaryNodeMilestone): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryNodeMilestoneRequiredCodeRule(var message: String) : PrimaryNodeMilestoneRule {
    override fun check(item: PrimaryNodeMilestone): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PrimaryNodeMilestoneAllowedCodeRule(var allowed: MutableList<String>) : PrimaryNodeMilestoneRule {
    override fun check(item: PrimaryNodeMilestone): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is inconsistent")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a primary node milestone. */
data class PrimaryNodeMilestoneValidator(var rules: MutableList<PrimaryNodeMilestoneRule>) {
    fun validate(item: PrimaryNodeMilestone): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: PrimaryNodeMilestone) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PrimaryNodeMilestone>): MutableMap<String, MutableList<String>> {
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

fun defaultPrimaryNodeMilestoneValidator(): PrimaryNodeMilestoneValidator {
    return PrimaryNodeMilestoneValidator(
        mutableListOf<PrimaryNodeMilestoneRule>(
            PrimaryNodeMilestoneNameLengthRule(120),
            PrimaryNodeMilestoneRequiredCodeRule("primary node milestone needs this field"),
            PrimaryNodeMilestoneAllowedCodeRule(mutableListOf<String>("rolling", "adaptive", "pending")),
        ),
    )
}
