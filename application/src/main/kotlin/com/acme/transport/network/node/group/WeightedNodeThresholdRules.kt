package com.acme.transport.network.node.group

import com.acme.transport.kit.text.normalize

/** A rule a weighted node threshold must pass. Check returns its problems, or none. */
interface WeightedNodeThresholdRule {
    fun check(item: WeightedNodeThreshold): MutableList<String>
}

data class WeightedNodeThresholdAllowedCodeRule(var allowed: MutableList<String>) : WeightedNodeThresholdRule {
    override fun check(item: WeightedNodeThreshold): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is expired")
        }
        return mutableListOf<String>()
    }
}

data class WeightedNodeThresholdLabelLengthRule(var maxLength: Int) : WeightedNodeThresholdRule {
    override fun check(item: WeightedNodeThreshold): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class WeightedNodeThresholdRequiredCodeRule(var message: String) : WeightedNodeThresholdRule {
    override fun check(item: WeightedNodeThreshold): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class WeightedNodeThresholdRequiredLabelRule(var message: String) : WeightedNodeThresholdRule {
    override fun check(item: WeightedNodeThreshold): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class WeightedNodeThresholdStartsAtRangeRule(var min: Int, var max: Int) : WeightedNodeThresholdRule {
    override fun check(item: WeightedNodeThreshold): MutableList<String> {
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

/** Runs every rule of a weighted node threshold. */
data class WeightedNodeThresholdValidator(var rules: MutableList<WeightedNodeThresholdRule>) {
    fun validate(item: WeightedNodeThreshold): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: WeightedNodeThreshold) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<WeightedNodeThreshold>): MutableMap<String, MutableList<String>> {
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

fun defaultWeightedNodeThresholdValidator(): WeightedNodeThresholdValidator {
    return WeightedNodeThresholdValidator(
        mutableListOf<WeightedNodeThresholdRule>(
            WeightedNodeThresholdAllowedCodeRule(mutableListOf<String>()),
            WeightedNodeThresholdLabelLengthRule(86400),
            WeightedNodeThresholdRequiredCodeRule("weighted node threshold needs this field"),
            WeightedNodeThresholdRequiredLabelRule("weighted node threshold needs an entry"),
            WeightedNodeThresholdStartsAtRangeRule(32, 3600),
        ),
    )
}
