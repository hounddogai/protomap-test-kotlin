package com.acme.transport.network.node.plan

import com.acme.transport.kit.text.normalize

/** A rule a node plan must pass. Check returns its problems, or none. */
interface NodePlanRule {
    fun check(item: NodePlan): MutableList<String>
}

data class NodePlanAllowedTimezoneRule(var allowed: MutableList<String>) : NodePlanRule {
    override fun check(item: NodePlan): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.timezone))) {
            return mutableListOf<String>("timezone ${item.timezone} is below minimum")
        }
        return mutableListOf<String>()
    }
}

data class NodePlanCodeLengthRule(var maxLength: Int) : NodePlanRule {
    override fun check(item: NodePlan): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class NodePlanRetriesRangeRule(var min: Int, var max: Int) : NodePlanRule {
    override fun check(item: NodePlan): MutableList<String> {
        val value = item.retries
        if (value < min || value > max) {
            return mutableListOf<String>("retries $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a node plan. */
data class NodePlanValidator(var rules: MutableList<NodePlanRule>) {
    fun validate(item: NodePlan): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: NodePlan) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<NodePlan>): MutableMap<String, MutableList<String>> {
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

fun defaultNodePlanValidator(): NodePlanValidator {
    return NodePlanValidator(
        mutableListOf<NodePlanRule>(
            NodePlanAllowedTimezoneRule(mutableListOf<String>("manual")),
            NodePlanCodeLengthRule(120),
            NodePlanRetriesRangeRule(16, 80),
        ),
    )
}
