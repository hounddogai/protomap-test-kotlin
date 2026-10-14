package com.acme.transport.terminal.traileryard.agreement

/** A rule a strict trailer yard checkpoint must pass. Check returns its problems, or none. */
interface StrictTrailerYardCheckpointRule {
    fun check(item: StrictTrailerYardCheckpoint): MutableList<String>
}

data class StrictTrailerYardCheckpointRequiredTitleRule(var message: String) : StrictTrailerYardCheckpointRule {
    override fun check(item: StrictTrailerYardCheckpoint): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class StrictTrailerYardCheckpointRequiredCarrierRule(
    var message: String,
) : StrictTrailerYardCheckpointRule {
    override fun check(item: StrictTrailerYardCheckpoint): MutableList<String> {
        if (item.carrier.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class StrictTrailerYardCheckpointRequiredCodeRule(var message: String) : StrictTrailerYardCheckpointRule {
    override fun check(item: StrictTrailerYardCheckpoint): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class StrictTrailerYardCheckpointSequenceRangeRule(
    var min: Int,
    var max: Int,
) : StrictTrailerYardCheckpointRule {
    override fun check(item: StrictTrailerYardCheckpoint): MutableList<String> {
        val value = item.sequence
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("sequence $value is below $min")
        }
        if (value > max) {
            problems.add("sequence $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a strict trailer yard checkpoint. */
data class StrictTrailerYardCheckpointValidator(var rules: MutableList<StrictTrailerYardCheckpointRule>) {
    fun validate(item: StrictTrailerYardCheckpoint): MutableList<String> {
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

    fun requireValid(item: StrictTrailerYardCheckpoint) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<StrictTrailerYardCheckpoint>): MutableMap<String, MutableList<String>> {
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

fun defaultStrictTrailerYardCheckpointValidator(): StrictTrailerYardCheckpointValidator {
    return StrictTrailerYardCheckpointValidator(
        mutableListOf<StrictTrailerYardCheckpointRule>(
            StrictTrailerYardCheckpointRequiredTitleRule("strict trailer yard checkpoint needs this field"),
            StrictTrailerYardCheckpointRequiredCarrierRule("strict trailer yard checkpoint needs this field"),
            StrictTrailerYardCheckpointRequiredCodeRule("strict trailer yard checkpoint needs a value"),
            StrictTrailerYardCheckpointSequenceRangeRule(16, 255),
        ),
    )
}
