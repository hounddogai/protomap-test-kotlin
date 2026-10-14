package com.acme.transport.emissions.idling.target

/** A rule a idling checkpoint must pass. Check returns its problems, or none. */
interface IdlingCheckpointRule {
    fun check(item: IdlingCheckpoint): MutableList<String>
}

data class IdlingCheckpointCreatedAtRangeRule(var min: Int, var max: Int) : IdlingCheckpointRule {
    override fun check(item: IdlingCheckpoint): MutableList<String> {
        val value = item.createdAt
        if (value < min || value > max) {
            return mutableListOf<String>("created at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class IdlingCheckpointCodeLengthRule(var maxLength: Int) : IdlingCheckpointRule {
    override fun check(item: IdlingCheckpoint): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class IdlingCheckpointNotesLengthRule(var maxLength: Int) : IdlingCheckpointRule {
    override fun check(item: IdlingCheckpoint): MutableList<String> {
        val size = item.notes.length
        if (size > maxLength) {
            return mutableListOf<String>("notes is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class IdlingCheckpointRequiredNotesRule(var message: String) : IdlingCheckpointRule {
    override fun check(item: IdlingCheckpoint): MutableList<String> {
        if (item.notes.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class IdlingCheckpointRequiredCodeRule(var message: String) : IdlingCheckpointRule {
    override fun check(item: IdlingCheckpoint): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class IdlingCheckpointStopCountRangeRule(var min: Int, var max: Int) : IdlingCheckpointRule {
    override fun check(item: IdlingCheckpoint): MutableList<String> {
        val value = item.stopCount
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("stop count $value is below $min")
        }
        if (value > max) {
            problems.add("stop count $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a idling checkpoint. */
data class IdlingCheckpointValidator(var rules: MutableList<IdlingCheckpointRule>) {
    fun validate(item: IdlingCheckpoint): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: IdlingCheckpoint) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<IdlingCheckpoint>): MutableMap<String, MutableList<String>> {
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

fun defaultIdlingCheckpointValidator(): IdlingCheckpointValidator {
    return IdlingCheckpointValidator(
        mutableListOf<IdlingCheckpointRule>(
            IdlingCheckpointCreatedAtRangeRule(1000, 3600),
            IdlingCheckpointCodeLengthRule(64),
            IdlingCheckpointNotesLengthRule(64),
            IdlingCheckpointRequiredNotesRule("idling checkpoint needs this field"),
            IdlingCheckpointRequiredCodeRule("idling checkpoint needs a value"),
            IdlingCheckpointStopCountRangeRule(16, 16),
        ),
    )
}
