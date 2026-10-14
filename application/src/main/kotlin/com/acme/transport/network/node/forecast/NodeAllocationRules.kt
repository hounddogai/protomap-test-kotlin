package com.acme.transport.network.node.forecast

/** A rule a node allocation must pass. Check returns its problems, or none. */
interface NodeAllocationRule {
    fun check(item: NodeAllocation): MutableList<String>
}

data class NodeAllocationEndsAtRangeRule(var min: Int, var max: Int) : NodeAllocationRule {
    override fun check(item: NodeAllocation): MutableList<String> {
        val value = item.endsAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("ends at $value is below $min")
        }
        if (value > max) {
            problems.add("ends at $value is above $max")
        }
        return problems
    }
}

data class NodeAllocationRequiredDriverIdRule(var message: String) : NodeAllocationRule {
    override fun check(item: NodeAllocation): MutableList<String> {
        if (item.driverId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class NodeAllocationRevisionRangeRule(var min: Int, var max: Int) : NodeAllocationRule {
    override fun check(item: NodeAllocation): MutableList<String> {
        val value = item.revision
        if (value < min || value > max) {
            return mutableListOf<String>("revision $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class NodeAllocationRequiredNameRule(var message: String) : NodeAllocationRule {
    override fun check(item: NodeAllocation): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class NodeAllocationBatchSizeRangeRule(var min: Int, var max: Int) : NodeAllocationRule {
    override fun check(item: NodeAllocation): MutableList<String> {
        val value = item.batchSize
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("batch size $value is below $min")
        }
        if (value > max) {
            problems.add("batch size $value is above $max")
        }
        return problems
    }
}

data class NodeAllocationCodeLengthRule(var maxLength: Int) : NodeAllocationRule {
    override fun check(item: NodeAllocation): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a node allocation. */
data class NodeAllocationValidator(var rules: MutableList<NodeAllocationRule>) {
    fun validate(item: NodeAllocation): MutableList<String> {
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

    fun requireValid(item: NodeAllocation) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<NodeAllocation>): MutableMap<String, MutableList<String>> {
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

fun defaultNodeAllocationValidator(): NodeAllocationValidator {
    return NodeAllocationValidator(
        mutableListOf<NodeAllocationRule>(
            NodeAllocationEndsAtRangeRule(64, 86400),
            NodeAllocationRequiredDriverIdRule("node allocation needs a value"),
            NodeAllocationRevisionRangeRule(1000, 3600),
            NodeAllocationRequiredNameRule("node allocation needs a value"),
            NodeAllocationBatchSizeRangeRule(3600, 86400),
            NodeAllocationCodeLengthRule(120),
        ),
    )
}
