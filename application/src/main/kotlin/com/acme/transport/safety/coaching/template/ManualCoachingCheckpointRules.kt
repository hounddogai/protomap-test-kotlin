package com.acme.transport.safety.coaching.template

/** A rule a manual coaching checkpoint must pass. Check returns its problems, or none. */
interface ManualCoachingCheckpointRule {
    fun check(item: ManualCoachingCheckpoint): MutableList<String>
}

data class ManualCoachingCheckpointTagsLimitRule(
    var maxCount: Int,
    var banned: MutableList<String>,
) : ManualCoachingCheckpointRule {
    override fun check(item: ManualCoachingCheckpoint): MutableList<String> {
        val problems = mutableListOf<String>()
        if (item.tags.size > maxCount) {
            problems.add("has ${item.tags.size} tags, at most $maxCount allowed")
        }
        for (tag in item.tags) {
            if (banned.contains(tag)) {
                problems.add("tag $tag is not allowed")
            }
        }
        return problems
    }
}

data class ManualCoachingCheckpointLabelLengthRule(var maxLength: Int) : ManualCoachingCheckpointRule {
    override fun check(item: ManualCoachingCheckpoint): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ManualCoachingCheckpointExpiresAtRangeRule(
    var min: Int,
    var max: Int,
) : ManualCoachingCheckpointRule {
    override fun check(item: ManualCoachingCheckpoint): MutableList<String> {
        val value = item.expiresAt
        if (value < min || value > max) {
            return mutableListOf<String>("expires at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class ManualCoachingCheckpointWindowDaysRangeRule(
    var min: Int,
    var max: Int,
) : ManualCoachingCheckpointRule {
    override fun check(item: ManualCoachingCheckpoint): MutableList<String> {
        val value = item.windowDays
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("window days $value is below $min")
        }
        if (value > max) {
            problems.add("window days $value is above $max")
        }
        return problems
    }
}

data class ManualCoachingCheckpointActiveGuardRule(var limit: Int) : ManualCoachingCheckpointRule {
    override fun check(item: ManualCoachingCheckpoint): MutableList<String> {
        if (item.active && item.windowDays > limit) {
            return mutableListOf<String>("window days ${item.windowDays} exceeds $limit while active")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a manual coaching checkpoint. */
data class ManualCoachingCheckpointValidator(var rules: MutableList<ManualCoachingCheckpointRule>) {
    fun validate(item: ManualCoachingCheckpoint): MutableList<String> {
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

    fun requireValid(item: ManualCoachingCheckpoint) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ManualCoachingCheckpoint>): MutableMap<String, MutableList<String>> {
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

fun defaultManualCoachingCheckpointValidator(): ManualCoachingCheckpointValidator {
    return ManualCoachingCheckpointValidator(
        mutableListOf<ManualCoachingCheckpointRule>(
            ManualCoachingCheckpointTagsLimitRule(120, mutableListOf<String>("strict", "strict")),
            ManualCoachingCheckpointLabelLengthRule(1000),
            ManualCoachingCheckpointExpiresAtRangeRule(16, 3600),
            ManualCoachingCheckpointWindowDaysRangeRule(32, 64),
            ManualCoachingCheckpointActiveGuardRule(80),
        ),
    )
}
