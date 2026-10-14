package com.acme.transport.network.node.nodeagreement

/** A rule a node profile must pass. Check returns its problems, or none. */
interface NodeProfileRule {
    fun check(item: NodeProfile): MutableList<String>
}

data class NodeProfileTitleLengthRule(var maxLength: Int) : NodeProfileRule {
    override fun check(item: NodeProfile): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class NodeProfileRequiredCurrencyRule(var message: String) : NodeProfileRule {
    override fun check(item: NodeProfile): MutableList<String> {
        if (item.currency.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class NodeProfileCodePrefixRule(var prefix: String) : NodeProfileRule {
    override fun check(item: NodeProfile): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class NodeProfileRequiredTitleRule(var message: String) : NodeProfileRule {
    override fun check(item: NodeProfile): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class NodeProfileCurrencyLengthRule(var maxLength: Int) : NodeProfileRule {
    override fun check(item: NodeProfile): MutableList<String> {
        val size = item.currency.length
        if (size > maxLength) {
            return mutableListOf<String>("currency is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a node profile. */
data class NodeProfileValidator(var rules: MutableList<NodeProfileRule>) {
    fun validate(item: NodeProfile): MutableList<String> {
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

    fun requireValid(item: NodeProfile) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<NodeProfile>): MutableMap<String, MutableList<String>> {
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

fun defaultNodeProfileValidator(): NodeProfileValidator {
    return NodeProfileValidator(
        mutableListOf<NodeProfileRule>(
            NodeProfileTitleLengthRule(80),
            NodeProfileRequiredCurrencyRule("node profile needs an entry"),
            NodeProfileCodePrefixRule("Q"),
            NodeProfileRequiredTitleRule("node profile needs a value"),
            NodeProfileCurrencyLengthRule(32),
        ),
    )
}
