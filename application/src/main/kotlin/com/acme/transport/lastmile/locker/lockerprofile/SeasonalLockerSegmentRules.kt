package com.acme.transport.lastmile.locker.lockerprofile

/** A rule a seasonal locker segment must pass. Check returns its problems, or none. */
interface SeasonalLockerSegmentRule {
    fun check(item: SeasonalLockerSegment): MutableList<String>
}

data class SeasonalLockerSegmentVisibleGuardRule(var limit: Int) : SeasonalLockerSegmentRule {
    override fun check(item: SeasonalLockerSegment): MutableList<String> {
        if (item.visible && item.updatedAt > limit) {
            return mutableListOf<String>("updated at ${item.updatedAt} exceeds $limit while visible")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalLockerSegmentUpdatedAtRangeRule(var min: Int, var max: Int) : SeasonalLockerSegmentRule {
    override fun check(item: SeasonalLockerSegment): MutableList<String> {
        val value = item.updatedAt
        if (value < min || value > max) {
            return mutableListOf<String>("updated at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalLockerSegmentNameLengthRule(var maxLength: Int) : SeasonalLockerSegmentRule {
    override fun check(item: SeasonalLockerSegment): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a seasonal locker segment. */
data class SeasonalLockerSegmentValidator(var rules: MutableList<SeasonalLockerSegmentRule>) {
    fun validate(item: SeasonalLockerSegment): MutableList<String> {
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

    fun requireValid(item: SeasonalLockerSegment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<SeasonalLockerSegment>): MutableMap<String, MutableList<String>> {
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

fun defaultSeasonalLockerSegmentValidator(): SeasonalLockerSegmentValidator {
    return SeasonalLockerSegmentValidator(
        mutableListOf<SeasonalLockerSegmentRule>(
            SeasonalLockerSegmentVisibleGuardRule(120),
            SeasonalLockerSegmentUpdatedAtRangeRule(64, 255),
            SeasonalLockerSegmentNameLengthRule(1000),
        ),
    )
}
