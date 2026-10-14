package com.acme.transport.linehaul.relay.checkpoint

/** A rule a relay checkpoint must pass. Check returns its problems, or none. */
interface RelayCheckpointRule {
    fun check(item: RelayCheckpoint): MutableList<String>
}

data class RelayCheckpointTitleLengthRule(var maxLength: Int) : RelayCheckpointRule {
    override fun check(item: RelayCheckpoint): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RelayCheckpointCapacityRangeRule(var min: Int, var max: Int) : RelayCheckpointRule {
    override fun check(item: RelayCheckpoint): MutableList<String> {
        val value = item.capacity
        if (value < min || value > max) {
            return mutableListOf<String>("capacity $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class RelayCheckpointDistanceKmRangeRule(var min: Double, var max: Double) : RelayCheckpointRule {
    override fun check(item: RelayCheckpoint): MutableList<String> {
        val value = item.distanceKm
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("distance km $value is below $min")
        }
        if (value > max) {
            problems.add("distance km $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a relay checkpoint. */
data class RelayCheckpointValidator(var rules: MutableList<RelayCheckpointRule>) {
    fun validate(item: RelayCheckpoint): MutableList<String> {
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

    fun requireValid(item: RelayCheckpoint) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RelayCheckpoint>): MutableMap<String, MutableList<String>> {
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

fun defaultRelayCheckpointValidator(): RelayCheckpointValidator {
    return RelayCheckpointValidator(
        mutableListOf<RelayCheckpointRule>(
            RelayCheckpointTitleLengthRule(120),
            RelayCheckpointCapacityRangeRule(16, 80),
            RelayCheckpointDistanceKmRangeRule(7.9, 3763.3),
        ),
    )
}
