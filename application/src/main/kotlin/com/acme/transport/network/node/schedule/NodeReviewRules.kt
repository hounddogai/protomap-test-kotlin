package com.acme.transport.network.node.schedule

/** A rule a node review must pass. Check returns its problems, or none. */
interface NodeReviewRule {
    fun check(item: NodeReview): MutableList<String>
}

data class NodeReviewCodeLengthRule(var maxLength: Int) : NodeReviewRule {
    override fun check(item: NodeReview): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class NodeReviewThresholdRangeRule(var min: Double, var max: Double) : NodeReviewRule {
    override fun check(item: NodeReview): MutableList<String> {
        val value = item.threshold
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("threshold $value is below $min")
        }
        if (value > max) {
            problems.add("threshold $value is above $max")
        }
        return problems
    }
}

data class NodeReviewDistanceKmRangeRule(var min: Double, var max: Double) : NodeReviewRule {
    override fun check(item: NodeReview): MutableList<String> {
        val value = item.distanceKm
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("distance km $value is below $min")
        }
        if (value > max) {
            problems.add("distance km $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a node review. */
data class NodeReviewValidator(var rules: MutableList<NodeReviewRule>) {
    fun validate(item: NodeReview): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: NodeReview) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<NodeReview>): MutableMap<String, MutableList<String>> {
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

fun defaultNodeReviewValidator(): NodeReviewValidator {
    return NodeReviewValidator(
        mutableListOf<NodeReviewRule>(
            NodeReviewCodeLengthRule(120),
            NodeReviewThresholdRangeRule(5.2, 1519.0),
            NodeReviewDistanceKmRangeRule(3.9, 110.1),
        ),
    )
}
