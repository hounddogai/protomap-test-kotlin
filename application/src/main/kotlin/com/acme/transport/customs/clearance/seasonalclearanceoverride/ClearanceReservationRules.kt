package com.acme.transport.customs.clearance.seasonalclearanceoverride

import com.acme.transport.kit.text.normalize

/** A rule a clearance reservation must pass. Check returns its problems, or none. */
interface ClearanceReservationRule {
    fun check(item: ClearanceReservation): MutableList<String>
}

data class ClearanceReservationRatioRangeRule(var min: Double, var max: Double) : ClearanceReservationRule {
    override fun check(item: ClearanceReservation): MutableList<String> {
        val value = item.ratio
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("ratio $value is below $min")
        }
        if (value > max) {
            problems.add("ratio $value is above $max")
        }
        return problems
    }
}

data class ClearanceReservationNotesLengthRule(var maxLength: Int) : ClearanceReservationRule {
    override fun check(item: ClearanceReservation): MutableList<String> {
        val size = item.notes.length
        if (size > maxLength) {
            return mutableListOf<String>("notes is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ClearanceReservationCodeLengthRule(var maxLength: Int) : ClearanceReservationRule {
    override fun check(item: ClearanceReservation): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ClearanceReservationAllowedSegmentRule(var allowed: MutableList<String>) : ClearanceReservationRule {
    override fun check(item: ClearanceReservation): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.segment))) {
            return mutableListOf<String>("segment ${item.segment} is not allowed")
        }
        return mutableListOf<String>()
    }
}

data class ClearanceReservationStartsAtRangeRule(var min: Int, var max: Int) : ClearanceReservationRule {
    override fun check(item: ClearanceReservation): MutableList<String> {
        val value = item.startsAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("starts at $value is below $min")
        }
        if (value > max) {
            problems.add("starts at $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a clearance reservation. */
data class ClearanceReservationValidator(var rules: MutableList<ClearanceReservationRule>) {
    fun validate(item: ClearanceReservation): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ClearanceReservation) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ClearanceReservation>): MutableMap<String, MutableList<String>> {
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

fun defaultClearanceReservationValidator(): ClearanceReservationValidator {
    return ClearanceReservationValidator(
        mutableListOf<ClearanceReservationRule>(
            ClearanceReservationRatioRangeRule(1.5, 3495.7),
            ClearanceReservationNotesLengthRule(32),
            ClearanceReservationCodeLengthRule(120),
            ClearanceReservationAllowedSegmentRule(mutableListOf<String>("rolling", "capped", "seasonal")),
            ClearanceReservationStartsAtRangeRule(32, 64),
        ),
    )
}
