package com.acme.transport.network.node.plan

/** A rule a node mapping must pass. Check returns its problems, or none. */
interface NodeMappingRule {
    fun check(item: NodeMapping): MutableList<String>
}

data class NodeMappingRequiredLabelRule(var message: String) : NodeMappingRule {
    override fun check(item: NodeMapping): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class NodeMappingRequiredCodeRule(var message: String) : NodeMappingRule {
    override fun check(item: NodeMapping): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class NodeMappingNameLengthRule(var maxLength: Int) : NodeMappingRule {
    override fun check(item: NodeMapping): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class NodeMappingRequiredNameRule(var message: String) : NodeMappingRule {
    override fun check(item: NodeMapping): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class NodeMappingScoreRangeRule(var min: Double, var max: Double) : NodeMappingRule {
    override fun check(item: NodeMapping): MutableList<String> {
        val value = item.score
        if (value < min || value > max) {
            return mutableListOf<String>("score $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class NodeMappingCapacityRangeRule(var min: Int, var max: Int) : NodeMappingRule {
    override fun check(item: NodeMapping): MutableList<String> {
        val value = item.capacity
        if (value < min || value > max) {
            return mutableListOf<String>("capacity $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a node mapping. */
data class NodeMappingValidator(var rules: MutableList<NodeMappingRule>) {
    fun validate(item: NodeMapping): MutableList<String> {
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

    fun requireValid(item: NodeMapping) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<NodeMapping>): MutableMap<String, MutableList<String>> {
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

fun defaultNodeMappingValidator(): NodeMappingValidator {
    return NodeMappingValidator(
        mutableListOf<NodeMappingRule>(
            NodeMappingRequiredLabelRule("node mapping needs an entry"),
            NodeMappingRequiredCodeRule("node mapping needs an entry"),
            NodeMappingNameLengthRule(80),
            NodeMappingRequiredNameRule("node mapping needs an entry"),
            NodeMappingScoreRangeRule(0.9, 804.7),
            NodeMappingCapacityRangeRule(16, 32),
        ),
    )
}
