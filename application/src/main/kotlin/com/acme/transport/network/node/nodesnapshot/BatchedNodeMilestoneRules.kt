package com.acme.transport.network.node.nodesnapshot

/** A rule a batched node milestone must pass. Check returns its problems, or none. */
interface BatchedNodeMilestoneRule {
    fun check(item: BatchedNodeMilestone): MutableList<String>
}

data class BatchedNodeMilestoneStartsAtRangeRule(var min: Int, var max: Int) : BatchedNodeMilestoneRule {
    override fun check(item: BatchedNodeMilestone): MutableList<String> {
        val value = item.startsAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("starts at $value is below $min")
        }
        if (value > max) {
            problems.add("starts at $value is above $max")
        }
        return problems
    }
}

data class BatchedNodeMilestoneFloorValueRangeRule(
    var min: Double,
    var max: Double,
) : BatchedNodeMilestoneRule {
    override fun check(item: BatchedNodeMilestone): MutableList<String> {
        val value = item.floorValue
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("floor value $value is below $min")
        }
        if (value > max) {
            problems.add("floor value $value is above $max")
        }
        return problems
    }
}

data class BatchedNodeMilestoneRequiredLaneRule(var message: String) : BatchedNodeMilestoneRule {
    override fun check(item: BatchedNodeMilestone): MutableList<String> {
        if (item.lane.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a batched node milestone. */
data class BatchedNodeMilestoneValidator(var rules: MutableList<BatchedNodeMilestoneRule>) {
    fun validate(item: BatchedNodeMilestone): MutableList<String> {
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

    fun requireValid(item: BatchedNodeMilestone) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<BatchedNodeMilestone>): MutableMap<String, MutableList<String>> {
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

fun defaultBatchedNodeMilestoneValidator(): BatchedNodeMilestoneValidator {
    return BatchedNodeMilestoneValidator(
        mutableListOf<BatchedNodeMilestoneRule>(
            BatchedNodeMilestoneStartsAtRangeRule(64, 3600),
            BatchedNodeMilestoneFloorValueRangeRule(3.9, 4287.6),
            BatchedNodeMilestoneRequiredLaneRule("batched node milestone needs an entry"),
        ),
    )
}
