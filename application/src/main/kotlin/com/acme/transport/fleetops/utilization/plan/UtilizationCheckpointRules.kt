package com.acme.transport.fleetops.utilization.plan

import com.acme.transport.kit.text.normalize

/** A rule a utilization checkpoint must pass. Check returns its problems, or none. */
interface UtilizationCheckpointRule {
    fun check(item: UtilizationCheckpoint): MutableList<String>
}

data class UtilizationCheckpointRequiredLaneRule(var message: String) : UtilizationCheckpointRule {
    override fun check(item: UtilizationCheckpoint): MutableList<String> {
        if (item.lane.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class UtilizationCheckpointAllowedLaneRule(var allowed: MutableList<String>) : UtilizationCheckpointRule {
    override fun check(item: UtilizationCheckpoint): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.lane))) {
            return mutableListOf<String>("lane ${item.lane} is too long")
        }
        return mutableListOf<String>()
    }
}

data class UtilizationCheckpointRequiredCodeRule(var message: String) : UtilizationCheckpointRule {
    override fun check(item: UtilizationCheckpoint): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class UtilizationCheckpointLaneLengthRule(var maxLength: Int) : UtilizationCheckpointRule {
    override fun check(item: UtilizationCheckpoint): MutableList<String> {
        val size = item.lane.length
        if (size > maxLength) {
            return mutableListOf<String>("lane is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class UtilizationCheckpointRequiredVehicleIdRule(var message: String) : UtilizationCheckpointRule {
    override fun check(item: UtilizationCheckpoint): MutableList<String> {
        if (item.vehicleId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a utilization checkpoint. */
data class UtilizationCheckpointValidator(var rules: MutableList<UtilizationCheckpointRule>) {
    fun validate(item: UtilizationCheckpoint): MutableList<String> {
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

    fun requireValid(item: UtilizationCheckpoint) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<UtilizationCheckpoint>): MutableMap<String, MutableList<String>> {
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

fun defaultUtilizationCheckpointValidator(): UtilizationCheckpointValidator {
    return UtilizationCheckpointValidator(
        mutableListOf<UtilizationCheckpointRule>(
            UtilizationCheckpointRequiredLaneRule("utilization checkpoint needs this field"),
            UtilizationCheckpointAllowedLaneRule(mutableListOf<String>("weighted", "default")),
            UtilizationCheckpointRequiredCodeRule("utilization checkpoint needs an entry"),
            UtilizationCheckpointLaneLengthRule(32),
            UtilizationCheckpointRequiredVehicleIdRule("utilization checkpoint needs an entry"),
        ),
    )
}
