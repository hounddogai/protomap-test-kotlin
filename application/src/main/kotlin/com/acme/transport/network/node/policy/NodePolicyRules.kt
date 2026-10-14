package com.acme.transport.network.node.policy

/** A rule a node policy must pass. Check returns its problems, or none. */
interface NodePolicyRule {
    fun check(item: NodePolicy): MutableList<String>
}

data class NodePolicyStopCountRangeRule(var min: Int, var max: Int) : NodePolicyRule {
    override fun check(item: NodePolicy): MutableList<String> {
        val value = item.stopCount
        if (value < min || value > max) {
            return mutableListOf<String>("stop count $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class NodePolicyTitleLengthRule(var maxLength: Int) : NodePolicyRule {
    override fun check(item: NodePolicy): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class NodePolicyRequiredCodeRule(var message: String) : NodePolicyRule {
    override fun check(item: NodePolicy): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class NodePolicySummaryLengthRule(var maxLength: Int) : NodePolicyRule {
    override fun check(item: NodePolicy): MutableList<String> {
        val size = item.summary.length
        if (size > maxLength) {
            return mutableListOf<String>("summary is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a node policy. */
data class NodePolicyValidator(var rules: MutableList<NodePolicyRule>) {
    fun validate(item: NodePolicy): MutableList<String> {
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

    fun requireValid(item: NodePolicy) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<NodePolicy>): MutableMap<String, MutableList<String>> {
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

fun defaultNodePolicyValidator(): NodePolicyValidator {
    return NodePolicyValidator(
        mutableListOf<NodePolicyRule>(
            NodePolicyStopCountRangeRule(16, 120),
            NodePolicyTitleLengthRule(32),
            NodePolicyRequiredCodeRule("node policy needs an entry"),
            NodePolicySummaryLengthRule(255),
        ),
    )
}
