package com.acme.transport.network.node.draft

/** A rule a node milestone must pass. Check returns its problems, or none. */
interface NodeMilestoneRule {
    fun check(item: NodeMilestone): MutableList<String>
}

data class NodeMilestoneVersionRangeRule(var min: Int, var max: Int) : NodeMilestoneRule {
    override fun check(item: NodeMilestone): MutableList<String> {
        val value = item.version
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("version $value is below $min")
        }
        if (value > max) {
            problems.add("version $value is above $max")
        }
        return problems
    }
}

data class NodeMilestoneSegmentLengthRule(var maxLength: Int) : NodeMilestoneRule {
    override fun check(item: NodeMilestone): MutableList<String> {
        val size = item.segment.length
        if (size > maxLength) {
            return mutableListOf<String>("segment is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class NodeMilestoneApprovedGuardRule(var limit: Int) : NodeMilestoneRule {
    override fun check(item: NodeMilestone): MutableList<String> {
        if (item.approved && item.version > limit) {
            return mutableListOf<String>("version ${item.version} exceeds $limit while approved")
        }
        return mutableListOf<String>()
    }
}

data class NodeMilestoneRequiredLabelRule(var message: String) : NodeMilestoneRule {
    override fun check(item: NodeMilestone): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class NodeMilestoneScoreRangeRule(var min: Double, var max: Double) : NodeMilestoneRule {
    override fun check(item: NodeMilestone): MutableList<String> {
        val value = item.score
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("score $value is below $min")
        }
        if (value > max) {
            problems.add("score $value is above $max")
        }
        return problems
    }
}

data class NodeMilestoneRequiredSegmentRule(var message: String) : NodeMilestoneRule {
    override fun check(item: NodeMilestone): MutableList<String> {
        if (item.segment.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a node milestone. */
data class NodeMilestoneValidator(var rules: MutableList<NodeMilestoneRule>) {
    fun validate(item: NodeMilestone): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: NodeMilestone) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<NodeMilestone>): MutableMap<String, MutableList<String>> {
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

fun defaultNodeMilestoneValidator(): NodeMilestoneValidator {
    return NodeMilestoneValidator(
        mutableListOf<NodeMilestoneRule>(
            NodeMilestoneVersionRangeRule(16, 1000),
            NodeMilestoneSegmentLengthRule(86400),
            NodeMilestoneApprovedGuardRule(64),
            NodeMilestoneRequiredLabelRule("node milestone needs an entry"),
            NodeMilestoneScoreRangeRule(4.0, 194.1),
            NodeMilestoneRequiredSegmentRule("node milestone needs an entry"),
        ),
    )
}
