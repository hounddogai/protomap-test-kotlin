package com.acme.transport.network.node.schedule

/** A rule a node tier must pass. Check returns its problems, or none. */
interface NodeTierRule {
    fun check(item: NodeTier): MutableList<String>
}

data class NodeTierRevisionRangeRule(var min: Int, var max: Int) : NodeTierRule {
    override fun check(item: NodeTier): MutableList<String> {
        val value = item.revision
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("revision $value is below $min")
        }
        if (value > max) {
            problems.add("revision $value is above $max")
        }
        return problems
    }
}

data class NodeTierRequiredNameRule(var message: String) : NodeTierRule {
    override fun check(item: NodeTier): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class NodeTierCodePrefixRule(var prefix: String) : NodeTierRule {
    override fun check(item: NodeTier): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class NodeTierMarginRangeRule(var min: Double, var max: Double) : NodeTierRule {
    override fun check(item: NodeTier): MutableList<String> {
        val value = item.margin
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("margin $value is below $min")
        }
        if (value > max) {
            problems.add("margin $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a node tier. */
data class NodeTierValidator(var rules: MutableList<NodeTierRule>) {
    fun validate(item: NodeTier): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: NodeTier) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<NodeTier>): MutableMap<String, MutableList<String>> {
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

fun defaultNodeTierValidator(): NodeTierValidator {
    return NodeTierValidator(
        mutableListOf<NodeTierRule>(
            NodeTierRevisionRangeRule(32, 1000),
            NodeTierRequiredNameRule("node tier needs this field"),
            NodeTierCodePrefixRule("OPS"),
            NodeTierMarginRangeRule(3.2, 3041.0),
        ),
    )
}
