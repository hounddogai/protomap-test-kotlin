package com.acme.transport.terminal.dwell.digest

/** A rule a strict dwell checkpoint must pass. Check returns its problems, or none. */
interface StrictDwellCheckpointRule {
    fun check(item: StrictDwellCheckpoint): MutableList<String>
}

data class StrictDwellCheckpointRetriesRangeRule(var min: Int, var max: Int) : StrictDwellCheckpointRule {
    override fun check(item: StrictDwellCheckpoint): MutableList<String> {
        val value = item.retries
        if (value < min || value > max) {
            return mutableListOf<String>("retries $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class StrictDwellCheckpointAmountRangeRule(var min: Double, var max: Double) : StrictDwellCheckpointRule {
    override fun check(item: StrictDwellCheckpoint): MutableList<String> {
        val value = item.amount
        if (value < min || value > max) {
            return mutableListOf<String>("amount $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class StrictDwellCheckpointCodePrefixRule(var prefix: String) : StrictDwellCheckpointRule {
    override fun check(item: StrictDwellCheckpoint): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a strict dwell checkpoint. */
data class StrictDwellCheckpointValidator(var rules: MutableList<StrictDwellCheckpointRule>) {
    fun validate(item: StrictDwellCheckpoint): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: StrictDwellCheckpoint) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<StrictDwellCheckpoint>): MutableMap<String, MutableList<String>> {
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

fun defaultStrictDwellCheckpointValidator(): StrictDwellCheckpointValidator {
    return StrictDwellCheckpointValidator(
        mutableListOf<StrictDwellCheckpointRule>(
            StrictDwellCheckpointRetriesRangeRule(255, 255),
            StrictDwellCheckpointAmountRangeRule(4.9, 4468.6),
            StrictDwellCheckpointCodePrefixRule("X"),
        ),
    )
}
