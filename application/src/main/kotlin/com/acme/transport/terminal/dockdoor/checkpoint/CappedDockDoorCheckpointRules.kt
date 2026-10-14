package com.acme.transport.terminal.dockdoor.checkpoint

/** A rule a capped dock door checkpoint must pass. Check returns its problems, or none. */
interface CappedDockDoorCheckpointRule {
    fun check(item: CappedDockDoorCheckpoint): MutableList<String>
}

data class CappedDockDoorCheckpointStopCountRangeRule(
    var min: Int,
    var max: Int,
) : CappedDockDoorCheckpointRule {
    override fun check(item: CappedDockDoorCheckpoint): MutableList<String> {
        val value = item.stopCount
        if (value < min || value > max) {
            return mutableListOf<String>("stop count $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class CappedDockDoorCheckpointRequiredLabelRule(var message: String) : CappedDockDoorCheckpointRule {
    override fun check(item: CappedDockDoorCheckpoint): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CappedDockDoorCheckpointRequiredTitleRule(var message: String) : CappedDockDoorCheckpointRule {
    override fun check(item: CappedDockDoorCheckpoint): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CappedDockDoorCheckpointCodeLengthRule(var maxLength: Int) : CappedDockDoorCheckpointRule {
    override fun check(item: CappedDockDoorCheckpoint): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CappedDockDoorCheckpointCodePrefixRule(var prefix: String) : CappedDockDoorCheckpointRule {
    override fun check(item: CappedDockDoorCheckpoint): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a capped dock door checkpoint. */
data class CappedDockDoorCheckpointValidator(var rules: MutableList<CappedDockDoorCheckpointRule>) {
    fun validate(item: CappedDockDoorCheckpoint): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: CappedDockDoorCheckpoint) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CappedDockDoorCheckpoint>): MutableMap<String, MutableList<String>> {
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

fun defaultCappedDockDoorCheckpointValidator(): CappedDockDoorCheckpointValidator {
    return CappedDockDoorCheckpointValidator(
        mutableListOf<CappedDockDoorCheckpointRule>(
            CappedDockDoorCheckpointStopCountRangeRule(32, 255),
            CappedDockDoorCheckpointRequiredLabelRule("capped dock door checkpoint needs a value"),
            CappedDockDoorCheckpointRequiredTitleRule("capped dock door checkpoint needs this field"),
            CappedDockDoorCheckpointCodeLengthRule(32),
            CappedDockDoorCheckpointCodePrefixRule("REF"),
        ),
    )
}
