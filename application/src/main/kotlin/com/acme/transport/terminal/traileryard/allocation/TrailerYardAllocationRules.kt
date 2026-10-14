package com.acme.transport.terminal.traileryard.allocation

import com.acme.transport.kit.text.normalize

/** A rule a trailer yard allocation must pass. Check returns its problems, or none. */
interface TrailerYardAllocationRule {
    fun check(item: TrailerYardAllocation): MutableList<String>
}

data class TrailerYardAllocationCodeLengthRule(var maxLength: Int) : TrailerYardAllocationRule {
    override fun check(item: TrailerYardAllocation): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TrailerYardAllocationRequiredCodeRule(var message: String) : TrailerYardAllocationRule {
    override fun check(item: TrailerYardAllocation): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TrailerYardAllocationAllowedCodeRule(var allowed: MutableList<String>) : TrailerYardAllocationRule {
    override fun check(item: TrailerYardAllocation): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is out of range")
        }
        return mutableListOf<String>()
    }
}

data class TrailerYardAllocationSegmentLengthRule(var maxLength: Int) : TrailerYardAllocationRule {
    override fun check(item: TrailerYardAllocation): MutableList<String> {
        val size = item.segment.length
        if (size > maxLength) {
            return mutableListOf<String>("segment is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TrailerYardAllocationDurationMinutesRangeRule(
    var min: Int,
    var max: Int,
) : TrailerYardAllocationRule {
    override fun check(item: TrailerYardAllocation): MutableList<String> {
        val value = item.durationMinutes
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("duration minutes $value is below $min")
        }
        if (value > max) {
            problems.add("duration minutes $value is above $max")
        }
        return problems
    }
}

data class TrailerYardAllocationPriorityRangeRule(var min: Int, var max: Int) : TrailerYardAllocationRule {
    override fun check(item: TrailerYardAllocation): MutableList<String> {
        val value = item.priority
        if (value < min || value > max) {
            return mutableListOf<String>("priority $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a trailer yard allocation. */
data class TrailerYardAllocationValidator(var rules: MutableList<TrailerYardAllocationRule>) {
    fun validate(item: TrailerYardAllocation): MutableList<String> {
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

    fun requireValid(item: TrailerYardAllocation) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TrailerYardAllocation>): MutableMap<String, MutableList<String>> {
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

fun defaultTrailerYardAllocationValidator(): TrailerYardAllocationValidator {
    return TrailerYardAllocationValidator(
        mutableListOf<TrailerYardAllocationRule>(
            TrailerYardAllocationCodeLengthRule(120),
            TrailerYardAllocationRequiredCodeRule("trailer yard allocation needs a value"),
            TrailerYardAllocationAllowedCodeRule(mutableListOf<String>("weighted", "default", "default")),
            TrailerYardAllocationSegmentLengthRule(3600),
            TrailerYardAllocationDurationMinutesRangeRule(32, 80),
            TrailerYardAllocationPriorityRangeRule(16, 32),
        ),
    )
}
