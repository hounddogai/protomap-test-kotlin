package com.acme.transport.terminal.sortation.sortationbaseline

/** A rule a pending sortation checkpoint must pass. Check returns its problems, or none. */
interface PendingSortationCheckpointRule {
    fun check(item: PendingSortationCheckpoint): MutableList<String>
}

data class PendingSortationCheckpointExternalRefLengthRule(
    var maxLength: Int,
) : PendingSortationCheckpointRule {
    override fun check(item: PendingSortationCheckpoint): MutableList<String> {
        val size = item.externalRef.length
        if (size > maxLength) {
            return mutableListOf<String>("external ref is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PendingSortationCheckpointRequiredLabelRule(var message: String) : PendingSortationCheckpointRule {
    override fun check(item: PendingSortationCheckpoint): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PendingSortationCheckpointCodePrefixRule(var prefix: String) : PendingSortationCheckpointRule {
    override fun check(item: PendingSortationCheckpoint): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class PendingSortationCheckpointCodeLengthRule(var maxLength: Int) : PendingSortationCheckpointRule {
    override fun check(item: PendingSortationCheckpoint): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a pending sortation checkpoint. */
data class PendingSortationCheckpointValidator(var rules: MutableList<PendingSortationCheckpointRule>) {
    fun validate(item: PendingSortationCheckpoint): MutableList<String> {
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

    fun requireValid(item: PendingSortationCheckpoint) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PendingSortationCheckpoint>): MutableMap<String, MutableList<String>> {
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

fun defaultPendingSortationCheckpointValidator(): PendingSortationCheckpointValidator {
    return PendingSortationCheckpointValidator(
        mutableListOf<PendingSortationCheckpointRule>(
            PendingSortationCheckpointExternalRefLengthRule(32),
            PendingSortationCheckpointRequiredLabelRule("pending sortation checkpoint needs a value"),
            PendingSortationCheckpointCodePrefixRule("Q"),
            PendingSortationCheckpointCodeLengthRule(255),
        ),
    )
}
