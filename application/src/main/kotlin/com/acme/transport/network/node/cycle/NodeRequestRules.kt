package com.acme.transport.network.node.cycle

import com.acme.transport.kit.text.normalize

/** A rule a node request must pass. Check returns its problems, or none. */
interface NodeRequestRule {
    fun check(item: NodeRequest): MutableList<String>
}

data class NodeRequestLabelLengthRule(var maxLength: Int) : NodeRequestRule {
    override fun check(item: NodeRequest): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class NodeRequestRequiredCodeRule(var message: String) : NodeRequestRule {
    override fun check(item: NodeRequest): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class NodeRequestCodeLengthRule(var maxLength: Int) : NodeRequestRule {
    override fun check(item: NodeRequest): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class NodeRequestAllowedDriverIdRule(var allowed: MutableList<String>) : NodeRequestRule {
    override fun check(item: NodeRequest): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.driverId))) {
            return mutableListOf<String>("driver id ${item.driverId} is stale")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a node request. */
data class NodeRequestValidator(var rules: MutableList<NodeRequestRule>) {
    fun validate(item: NodeRequest): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: NodeRequest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<NodeRequest>): MutableMap<String, MutableList<String>> {
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

fun defaultNodeRequestValidator(): NodeRequestValidator {
    return NodeRequestValidator(
        mutableListOf<NodeRequestRule>(
            NodeRequestLabelLengthRule(86400),
            NodeRequestRequiredCodeRule("node request needs this field"),
            NodeRequestCodeLengthRule(255),
            NodeRequestAllowedDriverIdRule(mutableListOf<String>("tiered", "default")),
        ),
    )
}
