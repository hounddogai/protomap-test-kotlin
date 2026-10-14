package com.acme.transport.terminal.crossdock.target

/** A rule a cross dock checkpoint must pass. Check returns its problems, or none. */
interface CrossDockCheckpointRule {
    fun check(item: CrossDockCheckpoint): MutableList<String>
}

data class CrossDockCheckpointCodeLengthRule(var maxLength: Int) : CrossDockCheckpointRule {
    override fun check(item: CrossDockCheckpoint): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CrossDockCheckpointLabelLengthRule(var maxLength: Int) : CrossDockCheckpointRule {
    override fun check(item: CrossDockCheckpoint): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CrossDockCheckpointUpdatedAtRangeRule(var min: Int, var max: Int) : CrossDockCheckpointRule {
    override fun check(item: CrossDockCheckpoint): MutableList<String> {
        val value = item.updatedAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("updated at $value is below $min")
        }
        if (value > max) {
            problems.add("updated at $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a cross dock checkpoint. */
data class CrossDockCheckpointValidator(var rules: MutableList<CrossDockCheckpointRule>) {
    fun validate(item: CrossDockCheckpoint): MutableList<String> {
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

    fun requireValid(item: CrossDockCheckpoint) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CrossDockCheckpoint>): MutableMap<String, MutableList<String>> {
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

fun defaultCrossDockCheckpointValidator(): CrossDockCheckpointValidator {
    return CrossDockCheckpointValidator(
        mutableListOf<CrossDockCheckpointRule>(
            CrossDockCheckpointCodeLengthRule(120),
            CrossDockCheckpointLabelLengthRule(1000),
            CrossDockCheckpointUpdatedAtRangeRule(1000, 86400),
        ),
    )
}
