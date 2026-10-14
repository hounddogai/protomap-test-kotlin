package com.acme.transport.safety.cameraevent.slot

import com.acme.transport.kit.text.normalize

/** A rule a camera event snapshot must pass. Check returns its problems, or none. */
interface CameraEventSnapshotRule {
    fun check(item: CameraEventSnapshot): MutableList<String>
}

data class CameraEventSnapshotAllowedVehicleIdRule(var allowed: MutableList<String>) : CameraEventSnapshotRule {
    override fun check(item: CameraEventSnapshot): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.vehicleId))) {
            return mutableListOf<String>("vehicle id ${item.vehicleId} is out of range")
        }
        return mutableListOf<String>()
    }
}

data class CameraEventSnapshotRequiredTitleRule(var message: String) : CameraEventSnapshotRule {
    override fun check(item: CameraEventSnapshot): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CameraEventSnapshotRetriesRangeRule(var min: Int, var max: Int) : CameraEventSnapshotRule {
    override fun check(item: CameraEventSnapshot): MutableList<String> {
        val value = item.retries
        if (value < min || value > max) {
            return mutableListOf<String>("retries $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a camera event snapshot. */
data class CameraEventSnapshotValidator(var rules: MutableList<CameraEventSnapshotRule>) {
    fun validate(item: CameraEventSnapshot): MutableList<String> {
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

    fun requireValid(item: CameraEventSnapshot) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CameraEventSnapshot>): MutableMap<String, MutableList<String>> {
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

fun defaultCameraEventSnapshotValidator(): CameraEventSnapshotValidator {
    return CameraEventSnapshotValidator(
        mutableListOf<CameraEventSnapshotRule>(
            CameraEventSnapshotAllowedVehicleIdRule(mutableListOf<String>("tiered", "primary", "manual")),
            CameraEventSnapshotRequiredTitleRule("camera event snapshot needs an entry"),
            CameraEventSnapshotRetriesRangeRule(80, 3600),
        ),
    )
}
