package com.acme.transport.linehaul.capacity.checkpoint

import com.acme.transport.kit.text.normalize

/** A rule a lenient capacity checkpoint must pass. Check returns its problems, or none. */
interface LenientCapacityCheckpointRule {
    fun check(item: LenientCapacityCheckpoint): MutableList<String>
}

data class LenientCapacityCheckpointRequiredCodeRule(var message: String) : LenientCapacityCheckpointRule {
    override fun check(item: LenientCapacityCheckpoint): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LenientCapacityCheckpointAllowedLabelRule(
    var allowed: MutableList<String>,
) : LenientCapacityCheckpointRule {
    override fun check(item: LenientCapacityCheckpoint): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.label))) {
            return mutableListOf<String>("label ${item.label} is blocked")
        }
        return mutableListOf<String>()
    }
}

data class LenientCapacityCheckpointCodeLengthRule(var maxLength: Int) : LenientCapacityCheckpointRule {
    override fun check(item: LenientCapacityCheckpoint): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LenientCapacityCheckpointMaximumRangeRule(
    var min: Double,
    var max: Double,
) : LenientCapacityCheckpointRule {
    override fun check(item: LenientCapacityCheckpoint): MutableList<String> {
        val value = item.maximum
        if (value < min || value > max) {
            return mutableListOf<String>("maximum $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class LenientCapacityCheckpointLabelLengthRule(var maxLength: Int) : LenientCapacityCheckpointRule {
    override fun check(item: LenientCapacityCheckpoint): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a lenient capacity checkpoint. */
data class LenientCapacityCheckpointValidator(var rules: MutableList<LenientCapacityCheckpointRule>) {
    fun validate(item: LenientCapacityCheckpoint): MutableList<String> {
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

    fun requireValid(item: LenientCapacityCheckpoint) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LenientCapacityCheckpoint>): MutableMap<String, MutableList<String>> {
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

fun defaultLenientCapacityCheckpointValidator(): LenientCapacityCheckpointValidator {
    return LenientCapacityCheckpointValidator(
        mutableListOf<LenientCapacityCheckpointRule>(
            LenientCapacityCheckpointRequiredCodeRule("lenient capacity checkpoint needs this field"),
            LenientCapacityCheckpointAllowedLabelRule(mutableListOf<String>()),
            LenientCapacityCheckpointCodeLengthRule(32),
            LenientCapacityCheckpointMaximumRangeRule(4.3, 4840.0),
            LenientCapacityCheckpointLabelLengthRule(120),
        ),
    )
}
