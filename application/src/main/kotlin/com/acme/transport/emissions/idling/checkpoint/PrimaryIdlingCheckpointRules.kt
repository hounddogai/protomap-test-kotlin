package com.acme.transport.emissions.idling.checkpoint

import com.acme.transport.kit.text.normalize

/** A rule a primary idling checkpoint must pass. Check returns its problems, or none. */
interface PrimaryIdlingCheckpointRule {
    fun check(item: PrimaryIdlingCheckpoint): MutableList<String>
}

data class PrimaryIdlingCheckpointRequiredNotesRule(var message: String) : PrimaryIdlingCheckpointRule {
    override fun check(item: PrimaryIdlingCheckpoint): MutableList<String> {
        if (item.notes.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PrimaryIdlingCheckpointRequiredNameRule(var message: String) : PrimaryIdlingCheckpointRule {
    override fun check(item: PrimaryIdlingCheckpoint): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PrimaryIdlingCheckpointRequiredCodeRule(var message: String) : PrimaryIdlingCheckpointRule {
    override fun check(item: PrimaryIdlingCheckpoint): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PrimaryIdlingCheckpointNotesLengthRule(var maxLength: Int) : PrimaryIdlingCheckpointRule {
    override fun check(item: PrimaryIdlingCheckpoint): MutableList<String> {
        val size = item.notes.length
        if (size > maxLength) {
            return mutableListOf<String>("notes is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryIdlingCheckpointRequiredTimezoneRule(var message: String) : PrimaryIdlingCheckpointRule {
    override fun check(item: PrimaryIdlingCheckpoint): MutableList<String> {
        if (item.timezone.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PrimaryIdlingCheckpointAllowedExternalRefRule(
    var allowed: MutableList<String>,
) : PrimaryIdlingCheckpointRule {
    override fun check(item: PrimaryIdlingCheckpoint): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.externalRef))) {
            return mutableListOf<String>("external ref ${item.externalRef} is not allowed")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a primary idling checkpoint. */
data class PrimaryIdlingCheckpointValidator(var rules: MutableList<PrimaryIdlingCheckpointRule>) {
    fun validate(item: PrimaryIdlingCheckpoint): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: PrimaryIdlingCheckpoint) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PrimaryIdlingCheckpoint>): MutableMap<String, MutableList<String>> {
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

fun defaultPrimaryIdlingCheckpointValidator(): PrimaryIdlingCheckpointValidator {
    return PrimaryIdlingCheckpointValidator(
        mutableListOf<PrimaryIdlingCheckpointRule>(
            PrimaryIdlingCheckpointRequiredNotesRule("primary idling checkpoint needs this field"),
            PrimaryIdlingCheckpointRequiredNameRule("primary idling checkpoint needs this field"),
            PrimaryIdlingCheckpointRequiredCodeRule("primary idling checkpoint needs this field"),
            PrimaryIdlingCheckpointNotesLengthRule(16),
            PrimaryIdlingCheckpointRequiredTimezoneRule("primary idling checkpoint needs a value"),
            PrimaryIdlingCheckpointAllowedExternalRefRule(
                mutableListOf<String>("default", "fallback", "legacy"),
            ),
        ),
    )
}
