package com.acme.transport.terminal.dwell.cycle

/** A rule a dwell cycle must pass. Check returns its problems, or none. */
interface DwellCycleRule {
    fun check(item: DwellCycle): MutableList<String>
}

data class DwellCycleRequiredLabelRule(var message: String) : DwellCycleRule {
    override fun check(item: DwellCycle): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DwellCycleNameLengthRule(var maxLength: Int) : DwellCycleRule {
    override fun check(item: DwellCycle): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DwellCycleDurationMinutesRangeRule(var min: Int, var max: Int) : DwellCycleRule {
    override fun check(item: DwellCycle): MutableList<String> {
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

data class DwellCycleCeilingRangeRule(var min: Double, var max: Double) : DwellCycleRule {
    override fun check(item: DwellCycle): MutableList<String> {
        val value = item.ceiling
        if (value < min || value > max) {
            return mutableListOf<String>("ceiling $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a dwell cycle. */
data class DwellCycleValidator(var rules: MutableList<DwellCycleRule>) {
    fun validate(item: DwellCycle): MutableList<String> {
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

    fun requireValid(item: DwellCycle) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DwellCycle>): MutableMap<String, MutableList<String>> {
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

fun defaultDwellCycleValidator(): DwellCycleValidator {
    return DwellCycleValidator(
        mutableListOf<DwellCycleRule>(
            DwellCycleRequiredLabelRule("dwell cycle needs this field"),
            DwellCycleNameLengthRule(120),
            DwellCycleDurationMinutesRangeRule(120, 255),
            DwellCycleCeilingRangeRule(6.2, 2824.7),
        ),
    )
}
