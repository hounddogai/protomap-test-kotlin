package com.acme.transport.network.node.draft

import com.acme.transport.kit.text.normalize

/** A rule a node draft must pass. Check returns its problems, or none. */
interface NodeDraftRule {
    fun check(item: NodeDraft): MutableList<String>
}

data class NodeDraftAllowedDescriptionRule(var allowed: MutableList<String>) : NodeDraftRule {
    override fun check(item: NodeDraft): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.description))) {
            return mutableListOf<String>("description ${item.description} is too long")
        }
        return mutableListOf<String>()
    }
}

data class NodeDraftRequiredNameRule(var message: String) : NodeDraftRule {
    override fun check(item: NodeDraft): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class NodeDraftRequiredCodeRule(var message: String) : NodeDraftRule {
    override fun check(item: NodeDraft): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class NodeDraftCodeLengthRule(var maxLength: Int) : NodeDraftRule {
    override fun check(item: NodeDraft): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class NodeDraftPalletsRangeRule(var min: Int, var max: Int) : NodeDraftRule {
    override fun check(item: NodeDraft): MutableList<String> {
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

data class NodeDraftNameLengthRule(var maxLength: Int) : NodeDraftRule {
    override fun check(item: NodeDraft): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a node draft. */
data class NodeDraftValidator(var rules: MutableList<NodeDraftRule>) {
    fun validate(item: NodeDraft): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: NodeDraft) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<NodeDraft>): MutableMap<String, MutableList<String>> {
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

fun defaultNodeDraftValidator(): NodeDraftValidator {
    return NodeDraftValidator(
        mutableListOf<NodeDraftRule>(
            NodeDraftAllowedDescriptionRule(mutableListOf<String>("pending", "weighted")),
            NodeDraftRequiredNameRule("node draft needs an entry"),
            NodeDraftRequiredCodeRule("node draft needs an entry"),
            NodeDraftCodeLengthRule(255),
            NodeDraftPalletsRangeRule(16, 3600),
            NodeDraftNameLengthRule(120),
        ),
    )
}
