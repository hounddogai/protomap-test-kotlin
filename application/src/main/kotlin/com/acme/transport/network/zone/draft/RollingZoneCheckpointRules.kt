package com.acme.transport.network.zone.draft

/** A rule a rolling zone checkpoint must pass. Check returns its problems, or none. */
interface RollingZoneCheckpointRule {
    fun check(item: RollingZoneCheckpoint): MutableList<String>
}

data class RollingZoneCheckpointVersionRangeRule(var min: Int, var max: Int) : RollingZoneCheckpointRule {
    override fun check(item: RollingZoneCheckpoint): MutableList<String> {
        val value = item.version
        if (value < min || value > max) {
            return mutableListOf<String>("version $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class RollingZoneCheckpointRequiredCodeRule(var message: String) : RollingZoneCheckpointRule {
    override fun check(item: RollingZoneCheckpoint): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RollingZoneCheckpointCodePrefixRule(var prefix: String) : RollingZoneCheckpointRule {
    override fun check(item: RollingZoneCheckpoint): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class RollingZoneCheckpointDurationMinutesRangeRule(
    var min: Int,
    var max: Int,
) : RollingZoneCheckpointRule {
    override fun check(item: RollingZoneCheckpoint): MutableList<String> {
        val value = item.durationMinutes
        if (value < min || value > max) {
            return mutableListOf<String>("duration minutes $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class RollingZoneCheckpointTitleLengthRule(var maxLength: Int) : RollingZoneCheckpointRule {
    override fun check(item: RollingZoneCheckpoint): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a rolling zone checkpoint. */
data class RollingZoneCheckpointValidator(var rules: MutableList<RollingZoneCheckpointRule>) {
    fun validate(item: RollingZoneCheckpoint): MutableList<String> {
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

    fun requireValid(item: RollingZoneCheckpoint) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RollingZoneCheckpoint>): MutableMap<String, MutableList<String>> {
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

fun defaultRollingZoneCheckpointValidator(): RollingZoneCheckpointValidator {
    return RollingZoneCheckpointValidator(
        mutableListOf<RollingZoneCheckpointRule>(
            RollingZoneCheckpointVersionRangeRule(80, 3600),
            RollingZoneCheckpointRequiredCodeRule("rolling zone checkpoint needs an entry"),
            RollingZoneCheckpointCodePrefixRule("Q"),
            RollingZoneCheckpointDurationMinutesRangeRule(32, 80),
            RollingZoneCheckpointTitleLengthRule(120),
        ),
    )
}
