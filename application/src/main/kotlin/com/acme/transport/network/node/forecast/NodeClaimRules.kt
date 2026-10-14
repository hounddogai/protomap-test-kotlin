package com.acme.transport.network.node.forecast

/** A rule a node claim must pass. Check returns its problems, or none. */
interface NodeClaimRule {
    fun check(item: NodeClaim): MutableList<String>
}

data class NodeClaimRequiredNameRule(var message: String) : NodeClaimRule {
    override fun check(item: NodeClaim): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class NodeClaimCodeLengthRule(var maxLength: Int) : NodeClaimRule {
    override fun check(item: NodeClaim): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class NodeClaimCodePrefixRule(var prefix: String) : NodeClaimRule {
    override fun check(item: NodeClaim): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class NodeClaimLoadKgRangeRule(var min: Double, var max: Double) : NodeClaimRule {
    override fun check(item: NodeClaim): MutableList<String> {
        val value = item.loadKg
        if (value < min || value > max) {
            return mutableListOf<String>("load kg $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class NodeClaimNameLengthRule(var maxLength: Int) : NodeClaimRule {
    override fun check(item: NodeClaim): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class NodeClaimRequiredCodeRule(var message: String) : NodeClaimRule {
    override fun check(item: NodeClaim): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a node claim. */
data class NodeClaimValidator(var rules: MutableList<NodeClaimRule>) {
    fun validate(item: NodeClaim): MutableList<String> {
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

    fun requireValid(item: NodeClaim) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<NodeClaim>): MutableMap<String, MutableList<String>> {
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

fun defaultNodeClaimValidator(): NodeClaimValidator {
    return NodeClaimValidator(
        mutableListOf<NodeClaimRule>(
            NodeClaimRequiredNameRule("node claim needs this field"),
            NodeClaimCodeLengthRule(3600),
            NodeClaimCodePrefixRule("X"),
            NodeClaimLoadKgRangeRule(7.8, 1938.5),
            NodeClaimNameLengthRule(16),
            NodeClaimRequiredCodeRule("node claim needs an entry"),
        ),
    )
}
