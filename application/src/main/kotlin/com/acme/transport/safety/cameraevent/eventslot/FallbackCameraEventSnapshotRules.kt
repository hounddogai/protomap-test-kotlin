package com.acme.transport.safety.cameraevent.eventslot

import com.acme.transport.kit.text.normalize

/** A rule a fallback camera event snapshot must pass. Check returns its problems, or none. */
interface FallbackCameraEventSnapshotRule {
    fun check(item: FallbackCameraEventSnapshot): MutableList<String>
}

data class FallbackCameraEventSnapshotRequiredRegionRule(
    var message: String,
) : FallbackCameraEventSnapshotRule {
    override fun check(item: FallbackCameraEventSnapshot): MutableList<String> {
        if (item.region.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FallbackCameraEventSnapshotRegionLengthRule(var maxLength: Int) : FallbackCameraEventSnapshotRule {
    override fun check(item: FallbackCameraEventSnapshot): MutableList<String> {
        val size = item.region.length
        if (size > maxLength) {
            return mutableListOf<String>("region is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FallbackCameraEventSnapshotRequiredLabelRule(var message: String) : FallbackCameraEventSnapshotRule {
    override fun check(item: FallbackCameraEventSnapshot): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FallbackCameraEventSnapshotRequiredCodeRule(var message: String) : FallbackCameraEventSnapshotRule {
    override fun check(item: FallbackCameraEventSnapshot): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FallbackCameraEventSnapshotAllowedTimezoneRule(
    var allowed: MutableList<String>,
) : FallbackCameraEventSnapshotRule {
    override fun check(item: FallbackCameraEventSnapshot): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.timezone))) {
            return mutableListOf<String>("timezone ${item.timezone} is above maximum")
        }
        return mutableListOf<String>()
    }
}

data class FallbackCameraEventSnapshotFloorValueRangeRule(
    var min: Double,
    var max: Double,
) : FallbackCameraEventSnapshotRule {
    override fun check(item: FallbackCameraEventSnapshot): MutableList<String> {
        val value = item.floorValue
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("floor value $value is below $min")
        }
        if (value > max) {
            problems.add("floor value $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a fallback camera event snapshot. */
data class FallbackCameraEventSnapshotValidator(var rules: MutableList<FallbackCameraEventSnapshotRule>) {
    fun validate(item: FallbackCameraEventSnapshot): MutableList<String> {
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

    fun requireValid(item: FallbackCameraEventSnapshot) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FallbackCameraEventSnapshot>): MutableMap<String, MutableList<String>> {
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

fun defaultFallbackCameraEventSnapshotValidator(): FallbackCameraEventSnapshotValidator {
    return FallbackCameraEventSnapshotValidator(
        mutableListOf<FallbackCameraEventSnapshotRule>(
            FallbackCameraEventSnapshotRequiredRegionRule("fallback camera event snapshot needs an entry"),
            FallbackCameraEventSnapshotRegionLengthRule(120),
            FallbackCameraEventSnapshotRequiredLabelRule("fallback camera event snapshot needs a value"),
            FallbackCameraEventSnapshotRequiredCodeRule("fallback camera event snapshot needs an entry"),
            FallbackCameraEventSnapshotAllowedTimezoneRule(mutableListOf<String>("fallback")),
            FallbackCameraEventSnapshotFloorValueRangeRule(3.0, 1054.7),
        ),
    )
}
