package com.acme.transport.network.node.schedule

/** A rule a node schedule must pass. Check returns its problems, or none. */
interface NodeScheduleRule {
    fun check(item: NodeSchedule): MutableList<String>
}

data class NodeScheduleLimitRangeRule(var min: Int, var max: Int) : NodeScheduleRule {
    override fun check(item: NodeSchedule): MutableList<String> {
        val value = item.limit
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("limit $value is below $min")
        }
        if (value > max) {
            problems.add("limit $value is above $max")
        }
        return problems
    }
}

data class NodeScheduleLabelLengthRule(var maxLength: Int) : NodeScheduleRule {
    override fun check(item: NodeSchedule): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class NodeScheduleCodeLengthRule(var maxLength: Int) : NodeScheduleRule {
    override fun check(item: NodeSchedule): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class NodeScheduleRequiredCodeRule(var message: String) : NodeScheduleRule {
    override fun check(item: NodeSchedule): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a node schedule. */
data class NodeScheduleValidator(var rules: MutableList<NodeScheduleRule>) {
    fun validate(item: NodeSchedule): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: NodeSchedule) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<NodeSchedule>): MutableMap<String, MutableList<String>> {
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

fun defaultNodeScheduleValidator(): NodeScheduleValidator {
    return NodeScheduleValidator(
        mutableListOf<NodeScheduleRule>(
            NodeScheduleLimitRangeRule(16, 120),
            NodeScheduleLabelLengthRule(255),
            NodeScheduleCodeLengthRule(64),
            NodeScheduleRequiredCodeRule("node schedule needs this field"),
        ),
    )
}
