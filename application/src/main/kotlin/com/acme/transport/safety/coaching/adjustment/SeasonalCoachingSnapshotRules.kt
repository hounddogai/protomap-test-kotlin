package com.acme.transport.safety.coaching.adjustment

/** A rule a seasonal coaching snapshot must pass. Check returns its problems, or none. */
interface SeasonalCoachingSnapshotRule {
    fun check(item: SeasonalCoachingSnapshot): MutableList<String>
}

data class SeasonalCoachingSnapshotPriorityRangeRule(
    var min: Int,
    var max: Int,
) : SeasonalCoachingSnapshotRule {
    override fun check(item: SeasonalCoachingSnapshot): MutableList<String> {
        val value = item.priority
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("priority $value is below $min")
        }
        if (value > max) {
            problems.add("priority $value is above $max")
        }
        return problems
    }
}

data class SeasonalCoachingSnapshotCodeLengthRule(var maxLength: Int) : SeasonalCoachingSnapshotRule {
    override fun check(item: SeasonalCoachingSnapshot): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalCoachingSnapshotChannelLengthRule(var maxLength: Int) : SeasonalCoachingSnapshotRule {
    override fun check(item: SeasonalCoachingSnapshot): MutableList<String> {
        val size = item.channel.length
        if (size > maxLength) {
            return mutableListOf<String>("channel is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a seasonal coaching snapshot. */
data class SeasonalCoachingSnapshotValidator(var rules: MutableList<SeasonalCoachingSnapshotRule>) {
    fun validate(item: SeasonalCoachingSnapshot): MutableList<String> {
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

    fun requireValid(item: SeasonalCoachingSnapshot) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<SeasonalCoachingSnapshot>): MutableMap<String, MutableList<String>> {
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

fun defaultSeasonalCoachingSnapshotValidator(): SeasonalCoachingSnapshotValidator {
    return SeasonalCoachingSnapshotValidator(
        mutableListOf<SeasonalCoachingSnapshotRule>(
            SeasonalCoachingSnapshotPriorityRangeRule(16, 64),
            SeasonalCoachingSnapshotCodeLengthRule(16),
            SeasonalCoachingSnapshotChannelLengthRule(255),
        ),
    )
}
