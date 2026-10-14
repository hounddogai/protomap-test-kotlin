package com.acme.transport.network.node.adjustment

/** A rule a weighted node window must pass. Check returns its problems, or none. */
interface WeightedNodeWindowRule {
    fun check(item: WeightedNodeWindow): MutableList<String>
}

data class WeightedNodeWindowSegmentLengthRule(var maxLength: Int) : WeightedNodeWindowRule {
    override fun check(item: WeightedNodeWindow): MutableList<String> {
        val size = item.segment.length
        if (size > maxLength) {
            return mutableListOf<String>("segment is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class WeightedNodeWindowNameLengthRule(var maxLength: Int) : WeightedNodeWindowRule {
    override fun check(item: WeightedNodeWindow): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class WeightedNodeWindowRequiredCodeRule(var message: String) : WeightedNodeWindowRule {
    override fun check(item: WeightedNodeWindow): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a weighted node window. */
data class WeightedNodeWindowValidator(var rules: MutableList<WeightedNodeWindowRule>) {
    fun validate(item: WeightedNodeWindow): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: WeightedNodeWindow) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<WeightedNodeWindow>): MutableMap<String, MutableList<String>> {
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

fun defaultWeightedNodeWindowValidator(): WeightedNodeWindowValidator {
    return WeightedNodeWindowValidator(
        mutableListOf<WeightedNodeWindowRule>(
            WeightedNodeWindowSegmentLengthRule(255),
            WeightedNodeWindowNameLengthRule(64),
            WeightedNodeWindowRequiredCodeRule("weighted node window needs an entry"),
        ),
    )
}
