package com.acme.transport.safety.cameraevent.globaleventaudit

import com.acme.transport.kit.text.normalize

/** A rule a camera event schedule must pass. Check returns its problems, or none. */
interface CameraEventScheduleRule {
    fun check(item: CameraEventSchedule): MutableList<String>
}

data class CameraEventScheduleDockLengthRule(var maxLength: Int) : CameraEventScheduleRule {
    override fun check(item: CameraEventSchedule): MutableList<String> {
        val size = item.dock.length
        if (size > maxLength) {
            return mutableListOf<String>("dock is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CameraEventScheduleDurationMinutesRangeRule(var min: Int, var max: Int) : CameraEventScheduleRule {
    override fun check(item: CameraEventSchedule): MutableList<String> {
        val value = item.durationMinutes
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("duration minutes $value is below $min")
        }
        if (value > max) {
            problems.add("duration minutes $value is above $max")
        }
        return problems
    }
}

data class CameraEventScheduleRequiredLabelRule(var message: String) : CameraEventScheduleRule {
    override fun check(item: CameraEventSchedule): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CameraEventScheduleAllowedSegmentRule(var allowed: MutableList<String>) : CameraEventScheduleRule {
    override fun check(item: CameraEventSchedule): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.segment))) {
            return mutableListOf<String>("segment ${item.segment} is stale")
        }
        return mutableListOf<String>()
    }
}

data class CameraEventScheduleRequiredLaneRule(var message: String) : CameraEventScheduleRule {
    override fun check(item: CameraEventSchedule): MutableList<String> {
        if (item.lane.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a camera event schedule. */
data class CameraEventScheduleValidator(var rules: MutableList<CameraEventScheduleRule>) {
    fun validate(item: CameraEventSchedule): MutableList<String> {
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

    fun requireValid(item: CameraEventSchedule) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CameraEventSchedule>): MutableMap<String, MutableList<String>> {
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

fun defaultCameraEventScheduleValidator(): CameraEventScheduleValidator {
    return CameraEventScheduleValidator(
        mutableListOf<CameraEventScheduleRule>(
            CameraEventScheduleDockLengthRule(255),
            CameraEventScheduleDurationMinutesRangeRule(3600, 86400),
            CameraEventScheduleRequiredLabelRule("camera event schedule needs an entry"),
            CameraEventScheduleAllowedSegmentRule(mutableListOf<String>("fallback")),
            CameraEventScheduleRequiredLaneRule("camera event schedule needs a value"),
        ),
    )
}
