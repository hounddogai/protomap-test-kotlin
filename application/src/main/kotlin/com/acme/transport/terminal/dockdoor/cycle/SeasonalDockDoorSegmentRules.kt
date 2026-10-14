package com.acme.transport.terminal.dockdoor.cycle

/** A rule a seasonal dock door segment must pass. Check returns its problems, or none. */
interface SeasonalDockDoorSegmentRule {
    fun check(item: SeasonalDockDoorSegment): MutableList<String>
}

data class SeasonalDockDoorSegmentRequiredTitleRule(var message: String) : SeasonalDockDoorSegmentRule {
    override fun check(item: SeasonalDockDoorSegment): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class SeasonalDockDoorSegmentEndsAtRangeRule(var min: Int, var max: Int) : SeasonalDockDoorSegmentRule {
    override fun check(item: SeasonalDockDoorSegment): MutableList<String> {
        val value = item.endsAt
        if (value < min || value > max) {
            return mutableListOf<String>("ends at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalDockDoorSegmentNameLengthRule(var maxLength: Int) : SeasonalDockDoorSegmentRule {
    override fun check(item: SeasonalDockDoorSegment): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalDockDoorSegmentRegionLengthRule(var maxLength: Int) : SeasonalDockDoorSegmentRule {
    override fun check(item: SeasonalDockDoorSegment): MutableList<String> {
        val size = item.region.length
        if (size > maxLength) {
            return mutableListOf<String>("region is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalDockDoorSegmentFloorValueRangeRule(
    var min: Double,
    var max: Double,
) : SeasonalDockDoorSegmentRule {
    override fun check(item: SeasonalDockDoorSegment): MutableList<String> {
        val value = item.floorValue
        if (value < min || value > max) {
            return mutableListOf<String>("floor value $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a seasonal dock door segment. */
data class SeasonalDockDoorSegmentValidator(var rules: MutableList<SeasonalDockDoorSegmentRule>) {
    fun validate(item: SeasonalDockDoorSegment): MutableList<String> {
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

    fun requireValid(item: SeasonalDockDoorSegment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<SeasonalDockDoorSegment>): MutableMap<String, MutableList<String>> {
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

fun defaultSeasonalDockDoorSegmentValidator(): SeasonalDockDoorSegmentValidator {
    return SeasonalDockDoorSegmentValidator(
        mutableListOf<SeasonalDockDoorSegmentRule>(
            SeasonalDockDoorSegmentRequiredTitleRule("seasonal dock door segment needs a value"),
            SeasonalDockDoorSegmentEndsAtRangeRule(32, 86400),
            SeasonalDockDoorSegmentNameLengthRule(16),
            SeasonalDockDoorSegmentRegionLengthRule(1000),
            SeasonalDockDoorSegmentFloorValueRangeRule(4.2, 73.1),
        ),
    )
}
