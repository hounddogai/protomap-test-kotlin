package com.acme.transport.customs.clearance.cycle

/** A rule a legacy clearance checkpoint must pass. Check returns its problems, or none. */
interface LegacyClearanceCheckpointRule {
    fun check(item: LegacyClearanceCheckpoint): MutableList<String>
}

data class LegacyClearanceCheckpointRequiredNameRule(var message: String) : LegacyClearanceCheckpointRule {
    override fun check(item: LegacyClearanceCheckpoint): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LegacyClearanceCheckpointLabelLengthRule(var maxLength: Int) : LegacyClearanceCheckpointRule {
    override fun check(item: LegacyClearanceCheckpoint): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LegacyClearanceCheckpointCodePrefixRule(var prefix: String) : LegacyClearanceCheckpointRule {
    override fun check(item: LegacyClearanceCheckpoint): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class LegacyClearanceCheckpointRequiredOwnerIdRule(var message: String) : LegacyClearanceCheckpointRule {
    override fun check(item: LegacyClearanceCheckpoint): MutableList<String> {
        if (item.ownerId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a legacy clearance checkpoint. */
data class LegacyClearanceCheckpointValidator(var rules: MutableList<LegacyClearanceCheckpointRule>) {
    fun validate(item: LegacyClearanceCheckpoint): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: LegacyClearanceCheckpoint) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LegacyClearanceCheckpoint>): MutableMap<String, MutableList<String>> {
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

fun defaultLegacyClearanceCheckpointValidator(): LegacyClearanceCheckpointValidator {
    return LegacyClearanceCheckpointValidator(
        mutableListOf<LegacyClearanceCheckpointRule>(
            LegacyClearanceCheckpointRequiredNameRule("legacy clearance checkpoint needs this field"),
            LegacyClearanceCheckpointLabelLengthRule(80),
            LegacyClearanceCheckpointCodePrefixRule("Q"),
            LegacyClearanceCheckpointRequiredOwnerIdRule("legacy clearance checkpoint needs an entry"),
        ),
    )
}
