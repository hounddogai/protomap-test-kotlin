package com.acme.transport.network.node.milestone

/** A rule a node batch must pass. Check returns its problems, or none. */
interface NodeBatchRule {
    fun check(item: NodeBatch): MutableList<String>
}

data class NodeBatchRequiredCarrierRule(var message: String) : NodeBatchRule {
    override fun check(item: NodeBatch): MutableList<String> {
        if (item.carrier.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class NodeBatchCarrierLengthRule(var maxLength: Int) : NodeBatchRule {
    override fun check(item: NodeBatch): MutableList<String> {
        val size = item.carrier.length
        if (size > maxLength) {
            return mutableListOf<String>("carrier is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class NodeBatchCodeLengthRule(var maxLength: Int) : NodeBatchRule {
    override fun check(item: NodeBatch): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class NodeBatchSourceLengthRule(var maxLength: Int) : NodeBatchRule {
    override fun check(item: NodeBatch): MutableList<String> {
        val size = item.source.length
        if (size > maxLength) {
            return mutableListOf<String>("source is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class NodeBatchRequiredTitleRule(var message: String) : NodeBatchRule {
    override fun check(item: NodeBatch): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a node batch. */
data class NodeBatchValidator(var rules: MutableList<NodeBatchRule>) {
    fun validate(item: NodeBatch): MutableList<String> {
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

    fun requireValid(item: NodeBatch) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<NodeBatch>): MutableMap<String, MutableList<String>> {
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

fun defaultNodeBatchValidator(): NodeBatchValidator {
    return NodeBatchValidator(
        mutableListOf<NodeBatchRule>(
            NodeBatchRequiredCarrierRule("node batch needs an entry"),
            NodeBatchCarrierLengthRule(1000),
            NodeBatchCodeLengthRule(3600),
            NodeBatchSourceLengthRule(32),
            NodeBatchRequiredTitleRule("node batch needs an entry"),
        ),
    )
}
