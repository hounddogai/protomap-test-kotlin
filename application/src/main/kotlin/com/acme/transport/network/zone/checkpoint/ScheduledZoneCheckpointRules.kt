package com.acme.transport.network.zone.checkpoint

/** A rule a scheduled zone checkpoint must pass. Check returns its problems, or none. */
interface ScheduledZoneCheckpointRule {
    fun check(item: ScheduledZoneCheckpoint): MutableList<String>
}

data class ScheduledZoneCheckpointShiftCodeLengthRule(var maxLength: Int) : ScheduledZoneCheckpointRule {
    override fun check(item: ScheduledZoneCheckpoint): MutableList<String> {
        val size = item.shiftCode.length
        if (size > maxLength) {
            return mutableListOf<String>("shift code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledZoneCheckpointRequiredShiftCodeRule(var message: String) : ScheduledZoneCheckpointRule {
    override fun check(item: ScheduledZoneCheckpoint): MutableList<String> {
        if (item.shiftCode.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ScheduledZoneCheckpointNameLengthRule(var maxLength: Int) : ScheduledZoneCheckpointRule {
    override fun check(item: ScheduledZoneCheckpoint): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledZoneCheckpointDistanceKmRangeRule(
    var min: Double,
    var max: Double,
) : ScheduledZoneCheckpointRule {
    override fun check(item: ScheduledZoneCheckpoint): MutableList<String> {
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

/** Runs every rule of a scheduled zone checkpoint. */
data class ScheduledZoneCheckpointValidator(var rules: MutableList<ScheduledZoneCheckpointRule>) {
    fun validate(item: ScheduledZoneCheckpoint): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ScheduledZoneCheckpoint) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ScheduledZoneCheckpoint>): MutableMap<String, MutableList<String>> {
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

fun defaultScheduledZoneCheckpointValidator(): ScheduledZoneCheckpointValidator {
    return ScheduledZoneCheckpointValidator(
        mutableListOf<ScheduledZoneCheckpointRule>(
            ScheduledZoneCheckpointShiftCodeLengthRule(16),
            ScheduledZoneCheckpointRequiredShiftCodeRule("scheduled zone checkpoint needs an entry"),
            ScheduledZoneCheckpointNameLengthRule(80),
            ScheduledZoneCheckpointDistanceKmRangeRule(9.3, 1071.9),
        ),
    )
}
