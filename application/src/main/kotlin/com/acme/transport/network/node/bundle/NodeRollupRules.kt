package com.acme.transport.network.node.bundle

/** A rule a node rollup must pass. Check returns its problems, or none. */
interface NodeRollupRule {
    fun check(item: NodeRollup): MutableList<String>
}

data class NodeRollupRequiredLabelRule(var message: String) : NodeRollupRule {
    override fun check(item: NodeRollup): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class NodeRollupRequiredDockRule(var message: String) : NodeRollupRule {
    override fun check(item: NodeRollup): MutableList<String> {
        if (item.dock.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class NodeRollupRequiredCodeRule(var message: String) : NodeRollupRule {
    override fun check(item: NodeRollup): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class NodeRollupMarginRangeRule(var min: Double, var max: Double) : NodeRollupRule {
    override fun check(item: NodeRollup): MutableList<String> {
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

/** Runs every rule of a node rollup. */
data class NodeRollupValidator(var rules: MutableList<NodeRollupRule>) {
    fun validate(item: NodeRollup): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: NodeRollup) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<NodeRollup>): MutableMap<String, MutableList<String>> {
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

fun defaultNodeRollupValidator(): NodeRollupValidator {
    return NodeRollupValidator(
        mutableListOf<NodeRollupRule>(
            NodeRollupRequiredLabelRule("node rollup needs an entry"),
            NodeRollupRequiredDockRule("node rollup needs this field"),
            NodeRollupRequiredCodeRule("node rollup needs an entry"),
            NodeRollupMarginRangeRule(8.5, 4625.8),
        ),
    )
}
