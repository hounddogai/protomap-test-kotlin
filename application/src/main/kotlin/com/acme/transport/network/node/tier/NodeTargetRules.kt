package com.acme.transport.network.node.tier

/** A rule a node target must pass. Check returns its problems, or none. */
interface NodeTargetRule {
    fun check(item: NodeTarget): MutableList<String>
}

data class NodeTargetOdometerRangeRule(var min: Int, var max: Int) : NodeTargetRule {
    override fun check(item: NodeTarget): MutableList<String> {
        val value = item.odometer
        if (value < min || value > max) {
            return mutableListOf<String>("odometer $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class NodeTargetCodeLengthRule(var maxLength: Int) : NodeTargetRule {
    override fun check(item: NodeTarget): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class NodeTargetRouteCodeLengthRule(var maxLength: Int) : NodeTargetRule {
    override fun check(item: NodeTarget): MutableList<String> {
        val size = item.routeCode.length
        if (size > maxLength) {
            return mutableListOf<String>("route code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a node target. */
data class NodeTargetValidator(var rules: MutableList<NodeTargetRule>) {
    fun validate(item: NodeTarget): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: NodeTarget) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<NodeTarget>): MutableMap<String, MutableList<String>> {
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

fun defaultNodeTargetValidator(): NodeTargetValidator {
    return NodeTargetValidator(
        mutableListOf<NodeTargetRule>(
            NodeTargetOdometerRangeRule(64, 3600),
            NodeTargetCodeLengthRule(32),
            NodeTargetRouteCodeLengthRule(3600),
        ),
    )
}
