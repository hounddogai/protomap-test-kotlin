package com.acme.transport.network.node.cycle

/** A rule a node digest must pass. Check returns its problems, or none. */
interface NodeDigestRule {
    fun check(item: NodeDigest): MutableList<String>
}

data class NodeDigestLoadKgRangeRule(var min: Double, var max: Double) : NodeDigestRule {
    override fun check(item: NodeDigest): MutableList<String> {
        val value = item.loadKg
        if (value < min || value > max) {
            return mutableListOf<String>("load kg $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class NodeDigestRequiredCodeRule(var message: String) : NodeDigestRule {
    override fun check(item: NodeDigest): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class NodeDigestMaximumRangeRule(var min: Double, var max: Double) : NodeDigestRule {
    override fun check(item: NodeDigest): MutableList<String> {
        val value = item.maximum
        if (value < min || value > max) {
            return mutableListOf<String>("maximum $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class NodeDigestCodeLengthRule(var maxLength: Int) : NodeDigestRule {
    override fun check(item: NodeDigest): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class NodeDigestQuantityRangeRule(var min: Int, var max: Int) : NodeDigestRule {
    override fun check(item: NodeDigest): MutableList<String> {
        val value = item.quantity
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("quantity $value is below $min")
        }
        if (value > max) {
            problems.add("quantity $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a node digest. */
data class NodeDigestValidator(var rules: MutableList<NodeDigestRule>) {
    fun validate(item: NodeDigest): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: NodeDigest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<NodeDigest>): MutableMap<String, MutableList<String>> {
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

fun defaultNodeDigestValidator(): NodeDigestValidator {
    return NodeDigestValidator(
        mutableListOf<NodeDigestRule>(
            NodeDigestLoadKgRangeRule(3.6, 4865.1),
            NodeDigestRequiredCodeRule("node digest needs an entry"),
            NodeDigestMaximumRangeRule(6.6, 3711.8),
            NodeDigestCodeLengthRule(80),
            NodeDigestQuantityRangeRule(16, 16),
        ),
    )
}
