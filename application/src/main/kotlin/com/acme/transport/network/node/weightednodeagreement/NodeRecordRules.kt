package com.acme.transport.network.node.weightednodeagreement

/** A rule a node record must pass. Check returns its problems, or none. */
interface NodeRecordRule {
    fun check(item: NodeRecord): MutableList<String>
}

data class NodeRecordNameLengthRule(var maxLength: Int) : NodeRecordRule {
    override fun check(item: NodeRecord): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class NodeRecordRequiredNameRule(var message: String) : NodeRecordRule {
    override fun check(item: NodeRecord): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class NodeRecordCapacityRangeRule(var min: Int, var max: Int) : NodeRecordRule {
    override fun check(item: NodeRecord): MutableList<String> {
        val value = item.capacity
        if (value < min || value > max) {
            return mutableListOf<String>("capacity $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class NodeRecordRequiredCodeRule(var message: String) : NodeRecordRule {
    override fun check(item: NodeRecord): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a node record. */
data class NodeRecordValidator(var rules: MutableList<NodeRecordRule>) {
    fun validate(item: NodeRecord): MutableList<String> {
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

    fun requireValid(item: NodeRecord) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<NodeRecord>): MutableMap<String, MutableList<String>> {
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

fun defaultNodeRecordValidator(): NodeRecordValidator {
    return NodeRecordValidator(
        mutableListOf<NodeRecordRule>(
            NodeRecordNameLengthRule(64),
            NodeRecordRequiredNameRule("node record needs a value"),
            NodeRecordCapacityRangeRule(120, 120),
            NodeRecordRequiredCodeRule("node record needs this field"),
        ),
    )
}
