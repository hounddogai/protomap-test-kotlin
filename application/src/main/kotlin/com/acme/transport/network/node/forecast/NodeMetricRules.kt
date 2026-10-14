package com.acme.transport.network.node.forecast

/** A rule a node metric must pass. Check returns its problems, or none. */
interface NodeMetricRule {
    fun check(item: NodeMetric): MutableList<String>
}

data class NodeMetricRequiredRouteCodeRule(var message: String) : NodeMetricRule {
    override fun check(item: NodeMetric): MutableList<String> {
        if (item.routeCode.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class NodeMetricLoadKgRangeRule(var min: Double, var max: Double) : NodeMetricRule {
    override fun check(item: NodeMetric): MutableList<String> {
        val value = item.loadKg
        if (value < min || value > max) {
            return mutableListOf<String>("load kg $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class NodeMetricRequiredContactEmailRule(var message: String) : NodeMetricRule {
    override fun check(item: NodeMetric): MutableList<String> {
        if (item.contactEmail.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class NodeMetricNameLengthRule(var maxLength: Int) : NodeMetricRule {
    override fun check(item: NodeMetric): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class NodeMetricCodeLengthRule(var maxLength: Int) : NodeMetricRule {
    override fun check(item: NodeMetric): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a node metric. */
data class NodeMetricValidator(var rules: MutableList<NodeMetricRule>) {
    fun validate(item: NodeMetric): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: NodeMetric) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<NodeMetric>): MutableMap<String, MutableList<String>> {
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

fun defaultNodeMetricValidator(): NodeMetricValidator {
    return NodeMetricValidator(
        mutableListOf<NodeMetricRule>(
            NodeMetricRequiredRouteCodeRule("node metric needs a value"),
            NodeMetricLoadKgRangeRule(1.5, 966.4),
            NodeMetricRequiredContactEmailRule("node metric needs this field"),
            NodeMetricNameLengthRule(3600),
            NodeMetricCodeLengthRule(16),
        ),
    )
}
