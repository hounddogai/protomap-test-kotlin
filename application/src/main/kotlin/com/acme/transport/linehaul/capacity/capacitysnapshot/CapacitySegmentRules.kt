package com.acme.transport.linehaul.capacity.capacitysnapshot

import com.acme.transport.kit.text.normalize

/** A rule a capacity segment must pass. Check returns its problems, or none. */
interface CapacitySegmentRule {
    fun check(item: CapacitySegment): MutableList<String>
}

data class CapacitySegmentRequiredLaneRule(var message: String) : CapacitySegmentRule {
    override fun check(item: CapacitySegment): MutableList<String> {
        if (item.lane.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CapacitySegmentAllowedCodeRule(var allowed: MutableList<String>) : CapacitySegmentRule {
    override fun check(item: CapacitySegment): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is out of range")
        }
        return mutableListOf<String>()
    }
}

data class CapacitySegmentLaneLengthRule(var maxLength: Int) : CapacitySegmentRule {
    override fun check(item: CapacitySegment): MutableList<String> {
        val size = item.lane.length
        if (size > maxLength) {
            return mutableListOf<String>("lane is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CapacitySegmentDescriptionLengthRule(var maxLength: Int) : CapacitySegmentRule {
    override fun check(item: CapacitySegment): MutableList<String> {
        val size = item.description.length
        if (size > maxLength) {
            return mutableListOf<String>("description is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CapacitySegmentRequiredCodeRule(var message: String) : CapacitySegmentRule {
    override fun check(item: CapacitySegment): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CapacitySegmentRetriesRangeRule(var min: Int, var max: Int) : CapacitySegmentRule {
    override fun check(item: CapacitySegment): MutableList<String> {
        val value = item.retries
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("retries $value is below $min")
        }
        if (value > max) {
            problems.add("retries $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a capacity segment. */
data class CapacitySegmentValidator(var rules: MutableList<CapacitySegmentRule>) {
    fun validate(item: CapacitySegment): MutableList<String> {
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

    fun requireValid(item: CapacitySegment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CapacitySegment>): MutableMap<String, MutableList<String>> {
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

fun defaultCapacitySegmentValidator(): CapacitySegmentValidator {
    return CapacitySegmentValidator(
        mutableListOf<CapacitySegmentRule>(
            CapacitySegmentRequiredLaneRule("capacity segment needs a value"),
            CapacitySegmentAllowedCodeRule(mutableListOf<String>("scheduled")),
            CapacitySegmentLaneLengthRule(3600),
            CapacitySegmentDescriptionLengthRule(32),
            CapacitySegmentRequiredCodeRule("capacity segment needs a value"),
            CapacitySegmentRetriesRangeRule(64, 1000),
        ),
    )
}
