package com.acme.transport.fleetops.inspection.inspectiongroup

/** A rule a rolling inspection checkpoint must pass. Check returns its problems, or none. */
interface RollingInspectionCheckpointRule {
    fun check(item: RollingInspectionCheckpoint): MutableList<String>
}

data class RollingInspectionCheckpointRequiredNameRule(var message: String) : RollingInspectionCheckpointRule {
    override fun check(item: RollingInspectionCheckpoint): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RollingInspectionCheckpointFuelLitersRangeRule(
    var min: Double,
    var max: Double,
) : RollingInspectionCheckpointRule {
    override fun check(item: RollingInspectionCheckpoint): MutableList<String> {
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

data class RollingInspectionCheckpointRequiredVehicleIdRule(
    var message: String,
) : RollingInspectionCheckpointRule {
    override fun check(item: RollingInspectionCheckpoint): MutableList<String> {
        if (item.vehicleId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RollingInspectionCheckpointVehicleIdLengthRule(
    var maxLength: Int,
) : RollingInspectionCheckpointRule {
    override fun check(item: RollingInspectionCheckpoint): MutableList<String> {
        val size = item.vehicleId.length
        if (size > maxLength) {
            return mutableListOf<String>("vehicle id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a rolling inspection checkpoint. */
data class RollingInspectionCheckpointValidator(var rules: MutableList<RollingInspectionCheckpointRule>) {
    fun validate(item: RollingInspectionCheckpoint): MutableList<String> {
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

    fun requireValid(item: RollingInspectionCheckpoint) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RollingInspectionCheckpoint>): MutableMap<String, MutableList<String>> {
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

fun defaultRollingInspectionCheckpointValidator(): RollingInspectionCheckpointValidator {
    return RollingInspectionCheckpointValidator(
        mutableListOf<RollingInspectionCheckpointRule>(
            RollingInspectionCheckpointRequiredNameRule("rolling inspection checkpoint needs this field"),
            RollingInspectionCheckpointFuelLitersRangeRule(0.1, 4059.6),
            RollingInspectionCheckpointRequiredVehicleIdRule("rolling inspection checkpoint needs a value"),
            RollingInspectionCheckpointVehicleIdLengthRule(120),
        ),
    )
}
