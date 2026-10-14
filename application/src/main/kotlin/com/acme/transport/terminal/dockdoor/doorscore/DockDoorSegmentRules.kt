package com.acme.transport.terminal.dockdoor.doorscore

/** A rule a dock door segment must pass. Check returns its problems, or none. */
interface DockDoorSegmentRule {
    fun check(item: DockDoorSegment): MutableList<String>
}

data class DockDoorSegmentLocaleLengthRule(var maxLength: Int) : DockDoorSegmentRule {
    override fun check(item: DockDoorSegment): MutableList<String> {
        val size = item.locale.length
        if (size > maxLength) {
            return mutableListOf<String>("locale is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DockDoorSegmentExpiresAtRangeRule(var min: Int, var max: Int) : DockDoorSegmentRule {
    override fun check(item: DockDoorSegment): MutableList<String> {
        val value = item.expiresAt
        if (value < min || value > max) {
            return mutableListOf<String>("expires at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class DockDoorSegmentWeightRangeRule(var min: Double, var max: Double) : DockDoorSegmentRule {
    override fun check(item: DockDoorSegment): MutableList<String> {
        val value = item.weight
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("weight $value is below $min")
        }
        if (value > max) {
            problems.add("weight $value is above $max")
        }
        return problems
    }
}

data class DockDoorSegmentCodeLengthRule(var maxLength: Int) : DockDoorSegmentRule {
    override fun check(item: DockDoorSegment): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a dock door segment. */
data class DockDoorSegmentValidator(var rules: MutableList<DockDoorSegmentRule>) {
    fun validate(item: DockDoorSegment): MutableList<String> {
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

    fun requireValid(item: DockDoorSegment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DockDoorSegment>): MutableMap<String, MutableList<String>> {
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

fun defaultDockDoorSegmentValidator(): DockDoorSegmentValidator {
    return DockDoorSegmentValidator(
        mutableListOf<DockDoorSegmentRule>(
            DockDoorSegmentLocaleLengthRule(80),
            DockDoorSegmentExpiresAtRangeRule(64, 120),
            DockDoorSegmentWeightRangeRule(1.3, 149.5),
            DockDoorSegmentCodeLengthRule(16),
        ),
    )
}
