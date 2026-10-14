package com.acme.transport.network.node.milestone

import com.acme.transport.kit.text.normalize

/** A rule a node threshold must pass. Check returns its problems, or none. */
interface NodeThresholdRule {
    fun check(item: NodeThreshold): MutableList<String>
}

data class NodeThresholdRequiredNameRule(var message: String) : NodeThresholdRule {
    override fun check(item: NodeThreshold): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class NodeThresholdPalletsRangeRule(var min: Int, var max: Int) : NodeThresholdRule {
    override fun check(item: NodeThreshold): MutableList<String> {
        val value = item.pallets
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("pallets $value is below $min")
        }
        if (value > max) {
            problems.add("pallets $value is above $max")
        }
        return problems
    }
}

data class NodeThresholdAllowedCodeRule(var allowed: MutableList<String>) : NodeThresholdRule {
    override fun check(item: NodeThreshold): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is blocked")
        }
        return mutableListOf<String>()
    }
}

data class NodeThresholdCodeLengthRule(var maxLength: Int) : NodeThresholdRule {
    override fun check(item: NodeThreshold): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class NodeThresholdCodePrefixRule(var prefix: String) : NodeThresholdRule {
    override fun check(item: NodeThreshold): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class NodeThresholdDurationMinutesRangeRule(var min: Int, var max: Int) : NodeThresholdRule {
    override fun check(item: NodeThreshold): MutableList<String> {
        val value = item.durationMinutes
        if (value < min || value > max) {
            return mutableListOf<String>("duration minutes $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a node threshold. */
data class NodeThresholdValidator(var rules: MutableList<NodeThresholdRule>) {
    fun validate(item: NodeThreshold): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: NodeThreshold) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<NodeThreshold>): MutableMap<String, MutableList<String>> {
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

fun defaultNodeThresholdValidator(): NodeThresholdValidator {
    return NodeThresholdValidator(
        mutableListOf<NodeThresholdRule>(
            NodeThresholdRequiredNameRule("node threshold needs an entry"),
            NodeThresholdPalletsRangeRule(64, 255),
            NodeThresholdAllowedCodeRule(mutableListOf<String>("default", "lenient")),
            NodeThresholdCodeLengthRule(1000),
            NodeThresholdCodePrefixRule("X"),
            NodeThresholdDurationMinutesRangeRule(32, 1000),
        ),
    )
}
