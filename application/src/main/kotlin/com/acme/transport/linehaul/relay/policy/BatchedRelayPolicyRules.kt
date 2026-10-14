package com.acme.transport.linehaul.relay.policy

/** A rule a batched relay policy must pass. Check returns its problems, or none. */
interface BatchedRelayPolicyRule {
    fun check(item: BatchedRelayPolicy): MutableList<String>
}

data class BatchedRelayPolicyRequiredCodeRule(var message: String) : BatchedRelayPolicyRule {
    override fun check(item: BatchedRelayPolicy): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class BatchedRelayPolicyCodeLengthRule(var maxLength: Int) : BatchedRelayPolicyRule {
    override fun check(item: BatchedRelayPolicy): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class BatchedRelayPolicyCeilingRangeRule(var min: Double, var max: Double) : BatchedRelayPolicyRule {
    override fun check(item: BatchedRelayPolicy): MutableList<String> {
        val value = item.ceiling
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("ceiling $value is below $min")
        }
        if (value > max) {
            problems.add("ceiling $value is above $max")
        }
        return problems
    }
}

data class BatchedRelayPolicyRequiredOwnerIdRule(var message: String) : BatchedRelayPolicyRule {
    override fun check(item: BatchedRelayPolicy): MutableList<String> {
        if (item.ownerId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a batched relay policy. */
data class BatchedRelayPolicyValidator(var rules: MutableList<BatchedRelayPolicyRule>) {
    fun validate(item: BatchedRelayPolicy): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: BatchedRelayPolicy) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<BatchedRelayPolicy>): MutableMap<String, MutableList<String>> {
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

fun defaultBatchedRelayPolicyValidator(): BatchedRelayPolicyValidator {
    return BatchedRelayPolicyValidator(
        mutableListOf<BatchedRelayPolicyRule>(
            BatchedRelayPolicyRequiredCodeRule("batched relay policy needs this field"),
            BatchedRelayPolicyCodeLengthRule(255),
            BatchedRelayPolicyCeilingRangeRule(2.5, 1377.1),
            BatchedRelayPolicyRequiredOwnerIdRule("batched relay policy needs a value"),
        ),
    )
}
