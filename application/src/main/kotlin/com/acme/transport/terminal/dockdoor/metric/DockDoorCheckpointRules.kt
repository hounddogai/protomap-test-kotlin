package com.acme.transport.terminal.dockdoor.metric

/** A rule a dock door checkpoint must pass. Check returns its problems, or none. */
interface DockDoorCheckpointRule {
    fun check(item: DockDoorCheckpoint): MutableList<String>
}

data class DockDoorCheckpointActiveGuardRule(var limit: Double) : DockDoorCheckpointRule {
    override fun check(item: DockDoorCheckpoint): MutableList<String> {
        if (item.active && item.budget > limit) {
            return mutableListOf<String>("budget ${item.budget} exceeds $limit while active")
        }
        return mutableListOf<String>()
    }
}

data class DockDoorCheckpointRequiredTrailerIdRule(var message: String) : DockDoorCheckpointRule {
    override fun check(item: DockDoorCheckpoint): MutableList<String> {
        if (item.trailerId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DockDoorCheckpointNameLengthRule(var maxLength: Int) : DockDoorCheckpointRule {
    override fun check(item: DockDoorCheckpoint): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DockDoorCheckpointRequiredCodeRule(var message: String) : DockDoorCheckpointRule {
    override fun check(item: DockDoorCheckpoint): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DockDoorCheckpointCodeLengthRule(var maxLength: Int) : DockDoorCheckpointRule {
    override fun check(item: DockDoorCheckpoint): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a dock door checkpoint. */
data class DockDoorCheckpointValidator(var rules: MutableList<DockDoorCheckpointRule>) {
    fun validate(item: DockDoorCheckpoint): MutableList<String> {
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

    fun requireValid(item: DockDoorCheckpoint) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DockDoorCheckpoint>): MutableMap<String, MutableList<String>> {
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

fun defaultDockDoorCheckpointValidator(): DockDoorCheckpointValidator {
    return DockDoorCheckpointValidator(
        mutableListOf<DockDoorCheckpointRule>(
            DockDoorCheckpointActiveGuardRule(9.2),
            DockDoorCheckpointRequiredTrailerIdRule("dock door checkpoint needs a value"),
            DockDoorCheckpointNameLengthRule(64),
            DockDoorCheckpointRequiredCodeRule("dock door checkpoint needs this field"),
            DockDoorCheckpointCodeLengthRule(120),
        ),
    )
}
