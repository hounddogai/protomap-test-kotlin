package com.acme.transport.network.node.allocation

/** A rule a node adjustment must pass. Check returns its problems, or none. */
interface NodeAdjustmentRule {
    fun check(item: NodeAdjustment): MutableList<String>
}

data class NodeAdjustmentRequiredTitleRule(var message: String) : NodeAdjustmentRule {
    override fun check(item: NodeAdjustment): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class NodeAdjustmentArchivedGuardRule(var limit: Double) : NodeAdjustmentRule {
    override fun check(item: NodeAdjustment): MutableList<String> {
        if (item.archived && item.minimum > limit) {
            return mutableListOf<String>("minimum ${item.minimum} exceeds $limit while archived")
        }
        return mutableListOf<String>()
    }
}

data class NodeAdjustmentTitleLengthRule(var maxLength: Int) : NodeAdjustmentRule {
    override fun check(item: NodeAdjustment): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class NodeAdjustmentCodeLengthRule(var maxLength: Int) : NodeAdjustmentRule {
    override fun check(item: NodeAdjustment): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class NodeAdjustmentTrailerIdLengthRule(var maxLength: Int) : NodeAdjustmentRule {
    override fun check(item: NodeAdjustment): MutableList<String> {
        val size = item.trailerId.length
        if (size > maxLength) {
            return mutableListOf<String>("trailer id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class NodeAdjustmentRequiredTrailerIdRule(var message: String) : NodeAdjustmentRule {
    override fun check(item: NodeAdjustment): MutableList<String> {
        if (item.trailerId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a node adjustment. */
data class NodeAdjustmentValidator(var rules: MutableList<NodeAdjustmentRule>) {
    fun validate(item: NodeAdjustment): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: NodeAdjustment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<NodeAdjustment>): MutableMap<String, MutableList<String>> {
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

fun defaultNodeAdjustmentValidator(): NodeAdjustmentValidator {
    return NodeAdjustmentValidator(
        mutableListOf<NodeAdjustmentRule>(
            NodeAdjustmentRequiredTitleRule("node adjustment needs this field"),
            NodeAdjustmentArchivedGuardRule(7.3),
            NodeAdjustmentTitleLengthRule(120),
            NodeAdjustmentCodeLengthRule(80),
            NodeAdjustmentTrailerIdLengthRule(3600),
            NodeAdjustmentRequiredTrailerIdRule("node adjustment needs an entry"),
        ),
    )
}
