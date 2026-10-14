package com.acme.transport.network.node.request

/** A rule a node baseline must pass. Check returns its problems, or none. */
interface NodeBaselineRule {
    fun check(item: NodeBaseline): MutableList<String>
}

data class NodeBaselineCodePrefixRule(var prefix: String) : NodeBaselineRule {
    override fun check(item: NodeBaseline): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class NodeBaselineMinimumRangeRule(var min: Double, var max: Double) : NodeBaselineRule {
    override fun check(item: NodeBaseline): MutableList<String> {
        val value = item.minimum
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("minimum $value is below $min")
        }
        if (value > max) {
            problems.add("minimum $value is above $max")
        }
        return problems
    }
}

data class NodeBaselineCapacityRangeRule(var min: Int, var max: Int) : NodeBaselineRule {
    override fun check(item: NodeBaseline): MutableList<String> {
        val value = item.capacity
        if (value < min || value > max) {
            return mutableListOf<String>("capacity $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class NodeBaselineRequiredChannelRule(var message: String) : NodeBaselineRule {
    override fun check(item: NodeBaseline): MutableList<String> {
        if (item.channel.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a node baseline. */
data class NodeBaselineValidator(var rules: MutableList<NodeBaselineRule>) {
    fun validate(item: NodeBaseline): MutableList<String> {
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

    fun requireValid(item: NodeBaseline) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<NodeBaseline>): MutableMap<String, MutableList<String>> {
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

fun defaultNodeBaselineValidator(): NodeBaselineValidator {
    return NodeBaselineValidator(
        mutableListOf<NodeBaselineRule>(
            NodeBaselineCodePrefixRule("REF"),
            NodeBaselineMinimumRangeRule(6.7, 933.5),
            NodeBaselineCapacityRangeRule(80, 120),
            NodeBaselineRequiredChannelRule("node baseline needs a value"),
        ),
    )
}
