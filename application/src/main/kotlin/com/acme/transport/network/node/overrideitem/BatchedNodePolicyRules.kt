package com.acme.transport.network.node.overrideitem

/** A rule a batched node policy must pass. Check returns its problems, or none. */
interface BatchedNodePolicyRule {
    fun check(item: BatchedNodePolicy): MutableList<String>
}

data class BatchedNodePolicyCodePrefixRule(var prefix: String) : BatchedNodePolicyRule {
    override fun check(item: BatchedNodePolicy): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class BatchedNodePolicyCodeLengthRule(var maxLength: Int) : BatchedNodePolicyRule {
    override fun check(item: BatchedNodePolicy): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class BatchedNodePolicyRequiredCodeRule(var message: String) : BatchedNodePolicyRule {
    override fun check(item: BatchedNodePolicy): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class BatchedNodePolicyRequiredLabelRule(var message: String) : BatchedNodePolicyRule {
    override fun check(item: BatchedNodePolicy): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a batched node policy. */
data class BatchedNodePolicyValidator(var rules: MutableList<BatchedNodePolicyRule>) {
    fun validate(item: BatchedNodePolicy): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: BatchedNodePolicy) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<BatchedNodePolicy>): MutableMap<String, MutableList<String>> {
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

fun defaultBatchedNodePolicyValidator(): BatchedNodePolicyValidator {
    return BatchedNodePolicyValidator(
        mutableListOf<BatchedNodePolicyRule>(
            BatchedNodePolicyCodePrefixRule("ACM"),
            BatchedNodePolicyCodeLengthRule(64),
            BatchedNodePolicyRequiredCodeRule("batched node policy needs an entry"),
            BatchedNodePolicyRequiredLabelRule("batched node policy needs a value"),
        ),
    )
}
