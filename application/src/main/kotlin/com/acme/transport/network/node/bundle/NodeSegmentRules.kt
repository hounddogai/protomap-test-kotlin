package com.acme.transport.network.node.bundle

import com.acme.transport.kit.text.normalize

/** A rule a node segment must pass. Check returns its problems, or none. */
interface NodeSegmentRule {
    fun check(item: NodeSegment): MutableList<String>
}

data class NodeSegmentBudgetRangeRule(var min: Double, var max: Double) : NodeSegmentRule {
    override fun check(item: NodeSegment): MutableList<String> {
        val value = item.budget
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("budget $value is below $min")
        }
        if (value > max) {
            problems.add("budget $value is above $max")
        }
        return problems
    }
}

data class NodeSegmentCategoryLengthRule(var maxLength: Int) : NodeSegmentRule {
    override fun check(item: NodeSegment): MutableList<String> {
        val size = item.category.length
        if (size > maxLength) {
            return mutableListOf<String>("category is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class NodeSegmentAllowedCodeRule(var allowed: MutableList<String>) : NodeSegmentRule {
    override fun check(item: NodeSegment): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is duplicated")
        }
        return mutableListOf<String>()
    }
}

data class NodeSegmentRequiredCodeRule(var message: String) : NodeSegmentRule {
    override fun check(item: NodeSegment): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a node segment. */
data class NodeSegmentValidator(var rules: MutableList<NodeSegmentRule>) {
    fun validate(item: NodeSegment): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: NodeSegment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<NodeSegment>): MutableMap<String, MutableList<String>> {
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

fun defaultNodeSegmentValidator(): NodeSegmentValidator {
    return NodeSegmentValidator(
        mutableListOf<NodeSegmentRule>(
            NodeSegmentBudgetRangeRule(8.6, 1085.0),
            NodeSegmentCategoryLengthRule(64),
            NodeSegmentAllowedCodeRule(mutableListOf<String>("batched", "pending", "default")),
            NodeSegmentRequiredCodeRule("node segment needs this field"),
        ),
    )
}
