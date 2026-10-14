package com.acme.transport.network.transittime.entry

/** A rule a transit time checkpoint must pass. Check returns its problems, or none. */
interface TransitTimeCheckpointRule {
    fun check(item: TransitTimeCheckpoint): MutableList<String>
}

data class TransitTimeCheckpointCodeLengthRule(var maxLength: Int) : TransitTimeCheckpointRule {
    override fun check(item: TransitTimeCheckpoint): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TransitTimeCheckpointTitleLengthRule(var maxLength: Int) : TransitTimeCheckpointRule {
    override fun check(item: TransitTimeCheckpoint): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TransitTimeCheckpointRequiredCodeRule(var message: String) : TransitTimeCheckpointRule {
    override fun check(item: TransitTimeCheckpoint): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TransitTimeCheckpointRequiredTitleRule(var message: String) : TransitTimeCheckpointRule {
    override fun check(item: TransitTimeCheckpoint): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TransitTimeCheckpointUpdatedAtRangeRule(var min: Int, var max: Int) : TransitTimeCheckpointRule {
    override fun check(item: TransitTimeCheckpoint): MutableList<String> {
        val value = item.updatedAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("updated at $value is below $min")
        }
        if (value > max) {
            problems.add("updated at $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a transit time checkpoint. */
data class TransitTimeCheckpointValidator(var rules: MutableList<TransitTimeCheckpointRule>) {
    fun validate(item: TransitTimeCheckpoint): MutableList<String> {
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

    fun requireValid(item: TransitTimeCheckpoint) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TransitTimeCheckpoint>): MutableMap<String, MutableList<String>> {
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

fun defaultTransitTimeCheckpointValidator(): TransitTimeCheckpointValidator {
    return TransitTimeCheckpointValidator(
        mutableListOf<TransitTimeCheckpointRule>(
            TransitTimeCheckpointCodeLengthRule(1000),
            TransitTimeCheckpointTitleLengthRule(86400),
            TransitTimeCheckpointRequiredCodeRule("transit time checkpoint needs this field"),
            TransitTimeCheckpointRequiredTitleRule("transit time checkpoint needs this field"),
            TransitTimeCheckpointUpdatedAtRangeRule(16, 120),
        ),
    )
}
