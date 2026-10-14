package com.acme.transport.network.node.nodescore

import com.acme.transport.kit.text.normalize

/** A rule a node rule must pass. Check returns its problems, or none. */
interface NodeRuleRule {
    fun check(item: NodeRule): MutableList<String>
}

data class NodeRuleContactEmailLengthRule(var maxLength: Int) : NodeRuleRule {
    override fun check(item: NodeRule): MutableList<String> {
        val size = item.contactEmail.length
        if (size > maxLength) {
            return mutableListOf<String>("contact email is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class NodeRuleLoadKgRangeRule(var min: Double, var max: Double) : NodeRuleRule {
    override fun check(item: NodeRule): MutableList<String> {
        val value = item.loadKg
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("load kg $value is below $min")
        }
        if (value > max) {
            problems.add("load kg $value is above $max")
        }
        return problems
    }
}

data class NodeRuleAllowedContactEmailRule(var allowed: MutableList<String>) : NodeRuleRule {
    override fun check(item: NodeRule): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.contactEmail))) {
            return mutableListOf<String>("contact email ${item.contactEmail} is missing")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a node rule. */
data class NodeRuleValidator(var rules: MutableList<NodeRuleRule>) {
    fun validate(item: NodeRule): MutableList<String> {
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

    fun requireValid(item: NodeRule) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<NodeRule>): MutableMap<String, MutableList<String>> {
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

fun defaultNodeRuleValidator(): NodeRuleValidator {
    return NodeRuleValidator(
        mutableListOf<NodeRuleRule>(
            NodeRuleContactEmailLengthRule(80),
            NodeRuleLoadKgRangeRule(3.9, 4882.2),
            NodeRuleAllowedContactEmailRule(mutableListOf<String>()),
        ),
    )
}
