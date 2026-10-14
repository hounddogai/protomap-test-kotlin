package com.acme.transport.network.node.policy

import com.acme.transport.kit.text.normalize

/** A rule a node window must pass. Check returns its problems, or none. */
interface NodeWindowRule {
    fun check(item: NodeWindow): MutableList<String>
}

data class NodeWindowRequiredCodeRule(var message: String) : NodeWindowRule {
    override fun check(item: NodeWindow): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class NodeWindowRequiredNameRule(var message: String) : NodeWindowRule {
    override fun check(item: NodeWindow): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class NodeWindowAllowedCodeRule(var allowed: MutableList<String>) : NodeWindowRule {
    override fun check(item: NodeWindow): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is below minimum")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a node window. */
data class NodeWindowValidator(var rules: MutableList<NodeWindowRule>) {
    fun validate(item: NodeWindow): MutableList<String> {
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

    fun requireValid(item: NodeWindow) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<NodeWindow>): MutableMap<String, MutableList<String>> {
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

fun defaultNodeWindowValidator(): NodeWindowValidator {
    return NodeWindowValidator(
        mutableListOf<NodeWindowRule>(
            NodeWindowRequiredCodeRule("node window needs a value"),
            NodeWindowRequiredNameRule("node window needs this field"),
            NodeWindowAllowedCodeRule(mutableListOf<String>("global", "manual")),
        ),
    )
}
