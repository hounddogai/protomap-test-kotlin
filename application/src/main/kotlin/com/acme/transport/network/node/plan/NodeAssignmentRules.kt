package com.acme.transport.network.node.plan

/** A rule a node assignment must pass. Check returns its problems, or none. */
interface NodeAssignmentRule {
    fun check(item: NodeAssignment): MutableList<String>
}

data class NodeAssignmentCodePrefixRule(var prefix: String) : NodeAssignmentRule {
    override fun check(item: NodeAssignment): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class NodeAssignmentRequiredVehicleIdRule(var message: String) : NodeAssignmentRule {
    override fun check(item: NodeAssignment): MutableList<String> {
        if (item.vehicleId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class NodeAssignmentCreatedAtRangeRule(var min: Int, var max: Int) : NodeAssignmentRule {
    override fun check(item: NodeAssignment): MutableList<String> {
        val value = item.createdAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("created at $value is below $min")
        }
        if (value > max) {
            problems.add("created at $value is above $max")
        }
        return problems
    }
}

data class NodeAssignmentRequiredLabelRule(var message: String) : NodeAssignmentRule {
    override fun check(item: NodeAssignment): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class NodeAssignmentWindowDaysRangeRule(var min: Int, var max: Int) : NodeAssignmentRule {
    override fun check(item: NodeAssignment): MutableList<String> {
        val value = item.windowDays
        if (value < min || value > max) {
            return mutableListOf<String>("window days $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class NodeAssignmentTitleLengthRule(var maxLength: Int) : NodeAssignmentRule {
    override fun check(item: NodeAssignment): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a node assignment. */
data class NodeAssignmentValidator(var rules: MutableList<NodeAssignmentRule>) {
    fun validate(item: NodeAssignment): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: NodeAssignment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<NodeAssignment>): MutableMap<String, MutableList<String>> {
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

fun defaultNodeAssignmentValidator(): NodeAssignmentValidator {
    return NodeAssignmentValidator(
        mutableListOf<NodeAssignmentRule>(
            NodeAssignmentCodePrefixRule("OPS"),
            NodeAssignmentRequiredVehicleIdRule("node assignment needs a value"),
            NodeAssignmentCreatedAtRangeRule(80, 1000),
            NodeAssignmentRequiredLabelRule("node assignment needs a value"),
            NodeAssignmentWindowDaysRangeRule(3600, 86400),
            NodeAssignmentTitleLengthRule(120),
        ),
    )
}
