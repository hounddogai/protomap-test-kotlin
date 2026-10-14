package com.acme.transport.network.node.nodepolicy

import com.acme.transport.kit.text.normalize

/** A rule a node bundle must pass. Check returns its problems, or none. */
interface NodeBundleRule {
    fun check(item: NodeBundle): MutableList<String>
}

data class NodeBundleCostRangeRule(var min: Double, var max: Double) : NodeBundleRule {
    override fun check(item: NodeBundle): MutableList<String> {
        val value = item.cost
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("cost $value is below $min")
        }
        if (value > max) {
            problems.add("cost $value is above $max")
        }
        return problems
    }
}

data class NodeBundleLabelLengthRule(var maxLength: Int) : NodeBundleRule {
    override fun check(item: NodeBundle): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class NodeBundleStartsAtRangeRule(var min: Int, var max: Int) : NodeBundleRule {
    override fun check(item: NodeBundle): MutableList<String> {
        val value = item.startsAt
        if (value < min || value > max) {
            return mutableListOf<String>("starts at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class NodeBundleAllowedCodeRule(var allowed: MutableList<String>) : NodeBundleRule {
    override fun check(item: NodeBundle): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is unknown")
        }
        return mutableListOf<String>()
    }
}

data class NodeBundleRequiredCodeRule(var message: String) : NodeBundleRule {
    override fun check(item: NodeBundle): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class NodeBundleCodeLengthRule(var maxLength: Int) : NodeBundleRule {
    override fun check(item: NodeBundle): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a node bundle. */
data class NodeBundleValidator(var rules: MutableList<NodeBundleRule>) {
    fun validate(item: NodeBundle): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: NodeBundle) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<NodeBundle>): MutableMap<String, MutableList<String>> {
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

fun defaultNodeBundleValidator(): NodeBundleValidator {
    return NodeBundleValidator(
        mutableListOf<NodeBundleRule>(
            NodeBundleCostRangeRule(5.4, 3694.3),
            NodeBundleLabelLengthRule(1000),
            NodeBundleStartsAtRangeRule(1000, 3600),
            NodeBundleAllowedCodeRule(mutableListOf<String>("pending", "legacy")),
            NodeBundleRequiredCodeRule("node bundle needs a value"),
            NodeBundleCodeLengthRule(3600),
        ),
    )
}
