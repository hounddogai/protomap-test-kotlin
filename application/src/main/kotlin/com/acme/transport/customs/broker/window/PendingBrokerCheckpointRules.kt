package com.acme.transport.customs.broker.window

/** A rule a pending broker checkpoint must pass. Check returns its problems, or none. */
interface PendingBrokerCheckpointRule {
    fun check(item: PendingBrokerCheckpoint): MutableList<String>
}

data class PendingBrokerCheckpointExternalRefLengthRule(var maxLength: Int) : PendingBrokerCheckpointRule {
    override fun check(item: PendingBrokerCheckpoint): MutableList<String> {
        val size = item.externalRef.length
        if (size > maxLength) {
            return mutableListOf<String>("external ref is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PendingBrokerCheckpointCeilingRangeRule(
    var min: Double,
    var max: Double,
) : PendingBrokerCheckpointRule {
    override fun check(item: PendingBrokerCheckpoint): MutableList<String> {
        val value = item.ceiling
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("ceiling $value is below $min")
        }
        if (value > max) {
            problems.add("ceiling $value is above $max")
        }
        return problems
    }
}

data class PendingBrokerCheckpointCodeLengthRule(var maxLength: Int) : PendingBrokerCheckpointRule {
    override fun check(item: PendingBrokerCheckpoint): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PendingBrokerCheckpointGraceDaysRangeRule(var min: Int, var max: Int) : PendingBrokerCheckpointRule {
    override fun check(item: PendingBrokerCheckpoint): MutableList<String> {
        val value = item.graceDays
        if (value < min || value > max) {
            return mutableListOf<String>("grace days $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a pending broker checkpoint. */
data class PendingBrokerCheckpointValidator(var rules: MutableList<PendingBrokerCheckpointRule>) {
    fun validate(item: PendingBrokerCheckpoint): MutableList<String> {
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

    fun requireValid(item: PendingBrokerCheckpoint) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PendingBrokerCheckpoint>): MutableMap<String, MutableList<String>> {
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

fun defaultPendingBrokerCheckpointValidator(): PendingBrokerCheckpointValidator {
    return PendingBrokerCheckpointValidator(
        mutableListOf<PendingBrokerCheckpointRule>(
            PendingBrokerCheckpointExternalRefLengthRule(64),
            PendingBrokerCheckpointCeilingRangeRule(0.3, 1632.1),
            PendingBrokerCheckpointCodeLengthRule(80),
            PendingBrokerCheckpointGraceDaysRangeRule(80, 255),
        ),
    )
}
