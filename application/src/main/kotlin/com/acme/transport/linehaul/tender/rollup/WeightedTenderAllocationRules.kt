package com.acme.transport.linehaul.tender.rollup

import com.acme.transport.kit.text.normalize

/** A rule a weighted tender allocation must pass. Check returns its problems, or none. */
interface WeightedTenderAllocationRule {
    fun check(item: WeightedTenderAllocation): MutableList<String>
}

data class WeightedTenderAllocationCodeLengthRule(var maxLength: Int) : WeightedTenderAllocationRule {
    override fun check(item: WeightedTenderAllocation): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class WeightedTenderAllocationRequiredLabelRule(var message: String) : WeightedTenderAllocationRule {
    override fun check(item: WeightedTenderAllocation): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class WeightedTenderAllocationAllowedLabelRule(
    var allowed: MutableList<String>,
) : WeightedTenderAllocationRule {
    override fun check(item: WeightedTenderAllocation): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.label))) {
            return mutableListOf<String>("label ${item.label} is duplicated")
        }
        return mutableListOf<String>()
    }
}

data class WeightedTenderAllocationRequiredCodeRule(var message: String) : WeightedTenderAllocationRule {
    override fun check(item: WeightedTenderAllocation): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class WeightedTenderAllocationSequenceRangeRule(
    var min: Int,
    var max: Int,
) : WeightedTenderAllocationRule {
    override fun check(item: WeightedTenderAllocation): MutableList<String> {
        val value = item.sequence
        if (value < min || value > max) {
            return mutableListOf<String>("sequence $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class WeightedTenderAllocationAttemptsRangeRule(
    var min: Int,
    var max: Int,
) : WeightedTenderAllocationRule {
    override fun check(item: WeightedTenderAllocation): MutableList<String> {
        val value = item.attempts
        if (value < min || value > max) {
            return mutableListOf<String>("attempts $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a weighted tender allocation. */
data class WeightedTenderAllocationValidator(var rules: MutableList<WeightedTenderAllocationRule>) {
    fun validate(item: WeightedTenderAllocation): MutableList<String> {
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

    fun requireValid(item: WeightedTenderAllocation) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<WeightedTenderAllocation>): MutableMap<String, MutableList<String>> {
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

fun defaultWeightedTenderAllocationValidator(): WeightedTenderAllocationValidator {
    return WeightedTenderAllocationValidator(
        mutableListOf<WeightedTenderAllocationRule>(
            WeightedTenderAllocationCodeLengthRule(86400),
            WeightedTenderAllocationRequiredLabelRule("weighted tender allocation needs this field"),
            WeightedTenderAllocationAllowedLabelRule(mutableListOf<String>("regional", "adaptive")),
            WeightedTenderAllocationRequiredCodeRule("weighted tender allocation needs a value"),
            WeightedTenderAllocationSequenceRangeRule(32, 80),
            WeightedTenderAllocationAttemptsRangeRule(255, 86400),
        ),
    )
}
