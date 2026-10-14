package com.acme.transport.safety.nearmiss.missvariance

/** A rule a near miss checkpoint must pass. Check returns its problems, or none. */
interface NearMissCheckpointRule {
    fun check(item: NearMissCheckpoint): MutableList<String>
}

data class NearMissCheckpointCodePrefixRule(var prefix: String) : NearMissCheckpointRule {
    override fun check(item: NearMissCheckpoint): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class NearMissCheckpointOwnerIdLengthRule(var maxLength: Int) : NearMissCheckpointRule {
    override fun check(item: NearMissCheckpoint): MutableList<String> {
        val size = item.ownerId.length
        if (size > maxLength) {
            return mutableListOf<String>("owner id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class NearMissCheckpointRequiredOwnerIdRule(var message: String) : NearMissCheckpointRule {
    override fun check(item: NearMissCheckpoint): MutableList<String> {
        if (item.ownerId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class NearMissCheckpointRequiredNotesRule(var message: String) : NearMissCheckpointRule {
    override fun check(item: NearMissCheckpoint): MutableList<String> {
        if (item.notes.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class NearMissCheckpointCodeLengthRule(var maxLength: Int) : NearMissCheckpointRule {
    override fun check(item: NearMissCheckpoint): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class NearMissCheckpointLabelLengthRule(var maxLength: Int) : NearMissCheckpointRule {
    override fun check(item: NearMissCheckpoint): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a near miss checkpoint. */
data class NearMissCheckpointValidator(var rules: MutableList<NearMissCheckpointRule>) {
    fun validate(item: NearMissCheckpoint): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: NearMissCheckpoint) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<NearMissCheckpoint>): MutableMap<String, MutableList<String>> {
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

fun defaultNearMissCheckpointValidator(): NearMissCheckpointValidator {
    return NearMissCheckpointValidator(
        mutableListOf<NearMissCheckpointRule>(
            NearMissCheckpointCodePrefixRule("X"),
            NearMissCheckpointOwnerIdLengthRule(32),
            NearMissCheckpointRequiredOwnerIdRule("near miss checkpoint needs a value"),
            NearMissCheckpointRequiredNotesRule("near miss checkpoint needs a value"),
            NearMissCheckpointCodeLengthRule(3600),
            NearMissCheckpointLabelLengthRule(64),
        ),
    )
}
