package com.acme.transport.freightpay.dispute.checkpoint

/** A rule a dispute checkpoint must pass. Check returns its problems, or none. */
interface DisputeCheckpointRule {
    fun check(item: DisputeCheckpoint): MutableList<String>
}

data class DisputeCheckpointCodePrefixRule(var prefix: String) : DisputeCheckpointRule {
    override fun check(item: DisputeCheckpoint): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class DisputeCheckpointLabelLengthRule(var maxLength: Int) : DisputeCheckpointRule {
    override fun check(item: DisputeCheckpoint): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DisputeCheckpointRequiredLabelRule(var message: String) : DisputeCheckpointRule {
    override fun check(item: DisputeCheckpoint): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DisputeCheckpointRequiredCodeRule(var message: String) : DisputeCheckpointRule {
    override fun check(item: DisputeCheckpoint): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DisputeCheckpointDriverIdLengthRule(var maxLength: Int) : DisputeCheckpointRule {
    override fun check(item: DisputeCheckpoint): MutableList<String> {
        val size = item.driverId.length
        if (size > maxLength) {
            return mutableListOf<String>("driver id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a dispute checkpoint. */
data class DisputeCheckpointValidator(var rules: MutableList<DisputeCheckpointRule>) {
    fun validate(item: DisputeCheckpoint): MutableList<String> {
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

    fun requireValid(item: DisputeCheckpoint) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DisputeCheckpoint>): MutableMap<String, MutableList<String>> {
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

fun defaultDisputeCheckpointValidator(): DisputeCheckpointValidator {
    return DisputeCheckpointValidator(
        mutableListOf<DisputeCheckpointRule>(
            DisputeCheckpointCodePrefixRule("REF"),
            DisputeCheckpointLabelLengthRule(86400),
            DisputeCheckpointRequiredLabelRule("dispute checkpoint needs an entry"),
            DisputeCheckpointRequiredCodeRule("dispute checkpoint needs an entry"),
            DisputeCheckpointDriverIdLengthRule(255),
        ),
    )
}
