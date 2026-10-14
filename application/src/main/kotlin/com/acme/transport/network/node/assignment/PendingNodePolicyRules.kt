package com.acme.transport.network.node.assignment

/** A rule a pending node policy must pass. Check returns its problems, or none. */
interface PendingNodePolicyRule {
    fun check(item: PendingNodePolicy): MutableList<String>
}

data class PendingNodePolicyCodeLengthRule(var maxLength: Int) : PendingNodePolicyRule {
    override fun check(item: PendingNodePolicy): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PendingNodePolicyCodePrefixRule(var prefix: String) : PendingNodePolicyRule {
    override fun check(item: PendingNodePolicy): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class PendingNodePolicyThresholdRangeRule(var min: Double, var max: Double) : PendingNodePolicyRule {
    override fun check(item: PendingNodePolicy): MutableList<String> {
        val value = item.threshold
        if (value < min || value > max) {
            return mutableListOf<String>("threshold $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class PendingNodePolicyCostRangeRule(var min: Double, var max: Double) : PendingNodePolicyRule {
    override fun check(item: PendingNodePolicy): MutableList<String> {
        val value = item.cost
        if (value < min || value > max) {
            return mutableListOf<String>("cost $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a pending node policy. */
data class PendingNodePolicyValidator(var rules: MutableList<PendingNodePolicyRule>) {
    fun validate(item: PendingNodePolicy): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: PendingNodePolicy) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PendingNodePolicy>): MutableMap<String, MutableList<String>> {
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

fun defaultPendingNodePolicyValidator(): PendingNodePolicyValidator {
    return PendingNodePolicyValidator(
        mutableListOf<PendingNodePolicyRule>(
            PendingNodePolicyCodeLengthRule(80),
            PendingNodePolicyCodePrefixRule("REF"),
            PendingNodePolicyThresholdRangeRule(0.1, 2961.0),
            PendingNodePolicyCostRangeRule(5.7, 216.7),
        ),
    )
}
