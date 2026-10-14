package com.acme.transport.network.hub.tieredhubplan

/** A rule a scheduled hub checkpoint must pass. Check returns its problems, or none. */
interface ScheduledHubCheckpointRule {
    fun check(item: ScheduledHubCheckpoint): MutableList<String>
}

data class ScheduledHubCheckpointRequiredLabelRule(var message: String) : ScheduledHubCheckpointRule {
    override fun check(item: ScheduledHubCheckpoint): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ScheduledHubCheckpointFuelLitersRangeRule(
    var min: Double,
    var max: Double,
) : ScheduledHubCheckpointRule {
    override fun check(item: ScheduledHubCheckpoint): MutableList<String> {
        val value = item.fuelLiters
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("fuel liters $value is below $min")
        }
        if (value > max) {
            problems.add("fuel liters $value is above $max")
        }
        return problems
    }
}

data class ScheduledHubCheckpointRatioRangeRule(var min: Double, var max: Double) : ScheduledHubCheckpointRule {
    override fun check(item: ScheduledHubCheckpoint): MutableList<String> {
        val value = item.ratio
        if (value < min || value > max) {
            return mutableListOf<String>("ratio $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a scheduled hub checkpoint. */
data class ScheduledHubCheckpointValidator(var rules: MutableList<ScheduledHubCheckpointRule>) {
    fun validate(item: ScheduledHubCheckpoint): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ScheduledHubCheckpoint) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ScheduledHubCheckpoint>): MutableMap<String, MutableList<String>> {
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

fun defaultScheduledHubCheckpointValidator(): ScheduledHubCheckpointValidator {
    return ScheduledHubCheckpointValidator(
        mutableListOf<ScheduledHubCheckpointRule>(
            ScheduledHubCheckpointRequiredLabelRule("scheduled hub checkpoint needs this field"),
            ScheduledHubCheckpointFuelLitersRangeRule(7.3, 1658.3),
            ScheduledHubCheckpointRatioRangeRule(1.9, 1368.9),
        ),
    )
}
