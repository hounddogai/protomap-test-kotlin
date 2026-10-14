package com.acme.transport.network.node.seasonalnodeagreement

/** A rule a node estimate must pass. Check returns its problems, or none. */
interface NodeEstimateRule {
    fun check(item: NodeEstimate): MutableList<String>
}

data class NodeEstimateMarginRangeRule(var min: Double, var max: Double) : NodeEstimateRule {
    override fun check(item: NodeEstimate): MutableList<String> {
        val value = item.margin
        if (value < min || value > max) {
            return mutableListOf<String>("margin $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class NodeEstimateRatioRangeRule(var min: Double, var max: Double) : NodeEstimateRule {
    override fun check(item: NodeEstimate): MutableList<String> {
        val value = item.ratio
        if (value < min || value > max) {
            return mutableListOf<String>("ratio $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class NodeEstimateRequiredSummaryRule(var message: String) : NodeEstimateRule {
    override fun check(item: NodeEstimate): MutableList<String> {
        if (item.summary.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class NodeEstimateCodePrefixRule(var prefix: String) : NodeEstimateRule {
    override fun check(item: NodeEstimate): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class NodeEstimateRequiredCodeRule(var message: String) : NodeEstimateRule {
    override fun check(item: NodeEstimate): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class NodeEstimateTagsLimitRule(var maxCount: Int, var banned: MutableList<String>) : NodeEstimateRule {
    override fun check(item: NodeEstimate): MutableList<String> {
        val problems = mutableListOf<String>()
        if (item.tags.size > maxCount) {
            problems.add("has ${item.tags.size} tags, at most $maxCount allowed")
        }
        for (tag in item.tags) {
            if (banned.contains(tag)) {
                problems.add("tag $tag is not allowed")
            }
        }
        return problems
    }
}

/** Runs every rule of a node estimate. */
data class NodeEstimateValidator(var rules: MutableList<NodeEstimateRule>) {
    fun validate(item: NodeEstimate): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: NodeEstimate) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<NodeEstimate>): MutableMap<String, MutableList<String>> {
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

fun defaultNodeEstimateValidator(): NodeEstimateValidator {
    return NodeEstimateValidator(
        mutableListOf<NodeEstimateRule>(
            NodeEstimateMarginRangeRule(8.5, 4862.3),
            NodeEstimateRatioRangeRule(2.6, 1508.0),
            NodeEstimateRequiredSummaryRule("node estimate needs a value"),
            NodeEstimateCodePrefixRule("REF"),
            NodeEstimateRequiredCodeRule("node estimate needs an entry"),
            NodeEstimateTagsLimitRule(32, mutableListOf<String>("tiered", "seasonal")),
        ),
    )
}
