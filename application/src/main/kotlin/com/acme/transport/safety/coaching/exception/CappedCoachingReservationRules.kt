package com.acme.transport.safety.coaching.exception

import com.acme.transport.kit.text.normalize

/** A rule a capped coaching reservation must pass. Check returns its problems, or none. */
interface CappedCoachingReservationRule {
    fun check(item: CappedCoachingReservation): MutableList<String>
}

data class CappedCoachingReservationCodeLengthRule(var maxLength: Int) : CappedCoachingReservationRule {
    override fun check(item: CappedCoachingReservation): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CappedCoachingReservationRequiredCategoryRule(var message: String) : CappedCoachingReservationRule {
    override fun check(item: CappedCoachingReservation): MutableList<String> {
        if (item.category.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CappedCoachingReservationStopCountRangeRule(
    var min: Int,
    var max: Int,
) : CappedCoachingReservationRule {
    override fun check(item: CappedCoachingReservation): MutableList<String> {
        val value = item.stopCount
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("stop count $value is below $min")
        }
        if (value > max) {
            problems.add("stop count $value is above $max")
        }
        return problems
    }
}

data class CappedCoachingReservationRequiredShiftCodeRule(var message: String) : CappedCoachingReservationRule {
    override fun check(item: CappedCoachingReservation): MutableList<String> {
        if (item.shiftCode.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CappedCoachingReservationAllowedCategoryRule(
    var allowed: MutableList<String>,
) : CappedCoachingReservationRule {
    override fun check(item: CappedCoachingReservation): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.category))) {
            return mutableListOf<String>("category ${item.category} is below minimum")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a capped coaching reservation. */
data class CappedCoachingReservationValidator(var rules: MutableList<CappedCoachingReservationRule>) {
    fun validate(item: CappedCoachingReservation): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: CappedCoachingReservation) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CappedCoachingReservation>): MutableMap<String, MutableList<String>> {
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

fun defaultCappedCoachingReservationValidator(): CappedCoachingReservationValidator {
    return CappedCoachingReservationValidator(
        mutableListOf<CappedCoachingReservationRule>(
            CappedCoachingReservationCodeLengthRule(255),
            CappedCoachingReservationRequiredCategoryRule("capped coaching reservation needs this field"),
            CappedCoachingReservationStopCountRangeRule(32, 86400),
            CappedCoachingReservationRequiredShiftCodeRule("capped coaching reservation needs an entry"),
            CappedCoachingReservationAllowedCategoryRule(
                mutableListOf<String>("scheduled", "lenient", "batched"),
            ),
        ),
    )
}
