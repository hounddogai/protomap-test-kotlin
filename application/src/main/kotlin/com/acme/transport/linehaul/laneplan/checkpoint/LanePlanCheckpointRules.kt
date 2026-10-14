package com.acme.transport.linehaul.laneplan.checkpoint

/** A rule a lane plan checkpoint must pass. Check returns its problems, or none. */
interface LanePlanCheckpointRule {
    fun check(item: LanePlanCheckpoint): MutableList<String>
}

data class LanePlanCheckpointRequiredLabelRule(var message: String) : LanePlanCheckpointRule {
    override fun check(item: LanePlanCheckpoint): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LanePlanCheckpointApprovedGuardRule(var limit: Int) : LanePlanCheckpointRule {
    override fun check(item: LanePlanCheckpoint): MutableList<String> {
        if (item.approved && item.stopCount > limit) {
            return mutableListOf<String>("stop count ${item.stopCount} exceeds $limit while approved")
        }
        return mutableListOf<String>()
    }
}

data class LanePlanCheckpointRequiredSegmentRule(var message: String) : LanePlanCheckpointRule {
    override fun check(item: LanePlanCheckpoint): MutableList<String> {
        if (item.segment.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LanePlanCheckpointDistanceKmRangeRule(var min: Double, var max: Double) : LanePlanCheckpointRule {
    override fun check(item: LanePlanCheckpoint): MutableList<String> {
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

data class LanePlanCheckpointUpdatedAtRangeRule(var min: Int, var max: Int) : LanePlanCheckpointRule {
    override fun check(item: LanePlanCheckpoint): MutableList<String> {
        val value = item.updatedAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("updated at $value is below $min")
        }
        if (value > max) {
            problems.add("updated at $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a lane plan checkpoint. */
data class LanePlanCheckpointValidator(var rules: MutableList<LanePlanCheckpointRule>) {
    fun validate(item: LanePlanCheckpoint): MutableList<String> {
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

    fun requireValid(item: LanePlanCheckpoint) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LanePlanCheckpoint>): MutableMap<String, MutableList<String>> {
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

fun defaultLanePlanCheckpointValidator(): LanePlanCheckpointValidator {
    return LanePlanCheckpointValidator(
        mutableListOf<LanePlanCheckpointRule>(
            LanePlanCheckpointRequiredLabelRule("lane plan checkpoint needs a value"),
            LanePlanCheckpointApprovedGuardRule(64),
            LanePlanCheckpointRequiredSegmentRule("lane plan checkpoint needs an entry"),
            LanePlanCheckpointDistanceKmRangeRule(7.6, 2636.1),
            LanePlanCheckpointUpdatedAtRangeRule(16, 32),
        ),
    )
}
