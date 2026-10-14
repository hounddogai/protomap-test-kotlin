package com.acme.transport.network.node.request

/** A rule a batched node request must pass. Check returns its problems, or none. */
interface BatchedNodeRequestRule {
    fun check(item: BatchedNodeRequest): MutableList<String>
}

data class BatchedNodeRequestRequiredChannelRule(var message: String) : BatchedNodeRequestRule {
    override fun check(item: BatchedNodeRequest): MutableList<String> {
        if (item.channel.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class BatchedNodeRequestCodePrefixRule(var prefix: String) : BatchedNodeRequestRule {
    override fun check(item: BatchedNodeRequest): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class BatchedNodeRequestNameLengthRule(var maxLength: Int) : BatchedNodeRequestRule {
    override fun check(item: BatchedNodeRequest): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a batched node request. */
data class BatchedNodeRequestValidator(var rules: MutableList<BatchedNodeRequestRule>) {
    fun validate(item: BatchedNodeRequest): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: BatchedNodeRequest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<BatchedNodeRequest>): MutableMap<String, MutableList<String>> {
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

fun defaultBatchedNodeRequestValidator(): BatchedNodeRequestValidator {
    return BatchedNodeRequestValidator(
        mutableListOf<BatchedNodeRequestRule>(
            BatchedNodeRequestRequiredChannelRule("batched node request needs an entry"),
            BatchedNodeRequestCodePrefixRule("OPS"),
            BatchedNodeRequestNameLengthRule(80),
        ),
    )
}
