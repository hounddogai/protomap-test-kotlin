package com.acme.transport.terminal.sortation.metric

/** A rule a tiered sortation checkpoint must pass. Check returns its problems, or none. */
interface TieredSortationCheckpointRule {
    fun check(item: TieredSortationCheckpoint): MutableList<String>
}

data class TieredSortationCheckpointMarginRangeRule(
    var min: Double,
    var max: Double,
) : TieredSortationCheckpointRule {
    override fun check(item: TieredSortationCheckpoint): MutableList<String> {
        val value = item.margin
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("margin $value is below $min")
        }
        if (value > max) {
            problems.add("margin $value is above $max")
        }
        return problems
    }
}

data class TieredSortationCheckpointTimezoneLengthRule(var maxLength: Int) : TieredSortationCheckpointRule {
    override fun check(item: TieredSortationCheckpoint): MutableList<String> {
        val size = item.timezone.length
        if (size > maxLength) {
            return mutableListOf<String>("timezone is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TieredSortationCheckpointFloorValueRangeRule(
    var min: Double,
    var max: Double,
) : TieredSortationCheckpointRule {
    override fun check(item: TieredSortationCheckpoint): MutableList<String> {
        val value = item.floorValue
        if (value < min || value > max) {
            return mutableListOf<String>("floor value $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a tiered sortation checkpoint. */
data class TieredSortationCheckpointValidator(var rules: MutableList<TieredSortationCheckpointRule>) {
    fun validate(item: TieredSortationCheckpoint): MutableList<String> {
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

    fun requireValid(item: TieredSortationCheckpoint) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TieredSortationCheckpoint>): MutableMap<String, MutableList<String>> {
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

fun defaultTieredSortationCheckpointValidator(): TieredSortationCheckpointValidator {
    return TieredSortationCheckpointValidator(
        mutableListOf<TieredSortationCheckpointRule>(
            TieredSortationCheckpointMarginRangeRule(5.3, 422.0),
            TieredSortationCheckpointTimezoneLengthRule(80),
            TieredSortationCheckpointFloorValueRangeRule(8.3, 4470.7),
        ),
    )
}
