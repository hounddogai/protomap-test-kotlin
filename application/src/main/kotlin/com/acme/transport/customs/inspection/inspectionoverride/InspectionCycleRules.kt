package com.acme.transport.customs.inspection.inspectionoverride

/** A rule a inspection cycle must pass. Check returns its problems, or none. */
interface InspectionCycleRule {
    fun check(item: InspectionCycle): MutableList<String>
}

data class InspectionCycleCodePrefixRule(var prefix: String) : InspectionCycleRule {
    override fun check(item: InspectionCycle): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class InspectionCycleCodeLengthRule(var maxLength: Int) : InspectionCycleRule {
    override fun check(item: InspectionCycle): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class InspectionCycleDurationMinutesRangeRule(var min: Int, var max: Int) : InspectionCycleRule {
    override fun check(item: InspectionCycle): MutableList<String> {
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

/** Runs every rule of a inspection cycle. */
data class InspectionCycleValidator(var rules: MutableList<InspectionCycleRule>) {
    fun validate(item: InspectionCycle): MutableList<String> {
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

    fun requireValid(item: InspectionCycle) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<InspectionCycle>): MutableMap<String, MutableList<String>> {
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

fun defaultInspectionCycleValidator(): InspectionCycleValidator {
    return InspectionCycleValidator(
        mutableListOf<InspectionCycleRule>(
            InspectionCycleCodePrefixRule("Q"),
            InspectionCycleCodeLengthRule(1000),
            InspectionCycleDurationMinutesRangeRule(64, 3600),
        ),
    )
}
