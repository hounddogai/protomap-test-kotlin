package com.acme.transport.safety.violation.checkpoint

/** A rule a violation checkpoint must pass. Check returns its problems, or none. */
interface ViolationCheckpointRule {
    fun check(item: ViolationCheckpoint): MutableList<String>
}

data class ViolationCheckpointPhoneLengthRule(var maxLength: Int) : ViolationCheckpointRule {
    override fun check(item: ViolationCheckpoint): MutableList<String> {
        val size = item.phone.length
        if (size > maxLength) {
            return mutableListOf<String>("phone is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ViolationCheckpointRequiredTitleRule(var message: String) : ViolationCheckpointRule {
    override fun check(item: ViolationCheckpoint): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ViolationCheckpointCodeLengthRule(var maxLength: Int) : ViolationCheckpointRule {
    override fun check(item: ViolationCheckpoint): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a violation checkpoint. */
data class ViolationCheckpointValidator(var rules: MutableList<ViolationCheckpointRule>) {
    fun validate(item: ViolationCheckpoint): MutableList<String> {
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

    fun requireValid(item: ViolationCheckpoint) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ViolationCheckpoint>): MutableMap<String, MutableList<String>> {
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

fun defaultViolationCheckpointValidator(): ViolationCheckpointValidator {
    return ViolationCheckpointValidator(
        mutableListOf<ViolationCheckpointRule>(
            ViolationCheckpointPhoneLengthRule(120),
            ViolationCheckpointRequiredTitleRule("violation checkpoint needs a value"),
            ViolationCheckpointCodeLengthRule(1000),
        ),
    )
}
