package com.acme.transport.safety.coaching.digest

import com.acme.transport.kit.text.normalize

/** A rule a coaching reservation must pass. Check returns its problems, or none. */
interface CoachingReservationRule {
    fun check(item: CoachingReservation): MutableList<String>
}

data class CoachingReservationAllowedNameRule(var allowed: MutableList<String>) : CoachingReservationRule {
    override fun check(item: CoachingReservation): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.name))) {
            return mutableListOf<String>("name ${item.name} is above maximum")
        }
        return mutableListOf<String>()
    }
}

data class CoachingReservationRequiredRouteCodeRule(var message: String) : CoachingReservationRule {
    override fun check(item: CoachingReservation): MutableList<String> {
        if (item.routeCode.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CoachingReservationRequiredTitleRule(var message: String) : CoachingReservationRule {
    override fun check(item: CoachingReservation): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CoachingReservationAmountRangeRule(var min: Double, var max: Double) : CoachingReservationRule {
    override fun check(item: CoachingReservation): MutableList<String> {
        val value = item.amount
        if (value < min || value > max) {
            return mutableListOf<String>("amount $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class CoachingReservationCodePrefixRule(var prefix: String) : CoachingReservationRule {
    override fun check(item: CoachingReservation): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a coaching reservation. */
data class CoachingReservationValidator(var rules: MutableList<CoachingReservationRule>) {
    fun validate(item: CoachingReservation): MutableList<String> {
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

    fun requireValid(item: CoachingReservation) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CoachingReservation>): MutableMap<String, MutableList<String>> {
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

fun defaultCoachingReservationValidator(): CoachingReservationValidator {
    return CoachingReservationValidator(
        mutableListOf<CoachingReservationRule>(
            CoachingReservationAllowedNameRule(mutableListOf<String>("pending", "pending")),
            CoachingReservationRequiredRouteCodeRule("coaching reservation needs this field"),
            CoachingReservationRequiredTitleRule("coaching reservation needs a value"),
            CoachingReservationAmountRangeRule(4.1, 4796.6),
            CoachingReservationCodePrefixRule("REF"),
        ),
    )
}
