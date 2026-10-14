package com.acme.transport.fleetops.inspection.reservation

import com.acme.transport.kit.text.normalize

/** A rule a inspection reservation must pass. Check returns its problems, or none. */
interface InspectionReservationRule {
    fun check(item: InspectionReservation): MutableList<String>
}

data class InspectionReservationAllowedCodeRule(var allowed: MutableList<String>) : InspectionReservationRule {
    override fun check(item: InspectionReservation): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is out of range")
        }
        return mutableListOf<String>()
    }
}

data class InspectionReservationCodePrefixRule(var prefix: String) : InspectionReservationRule {
    override fun check(item: InspectionReservation): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class InspectionReservationStartsAtRangeRule(var min: Int, var max: Int) : InspectionReservationRule {
    override fun check(item: InspectionReservation): MutableList<String> {
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

/** Runs every rule of a inspection reservation. */
data class InspectionReservationValidator(var rules: MutableList<InspectionReservationRule>) {
    fun validate(item: InspectionReservation): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: InspectionReservation) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<InspectionReservation>): MutableMap<String, MutableList<String>> {
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

fun defaultInspectionReservationValidator(): InspectionReservationValidator {
    return InspectionReservationValidator(
        mutableListOf<InspectionReservationRule>(
            InspectionReservationAllowedCodeRule(mutableListOf<String>("batched", "legacy")),
            InspectionReservationCodePrefixRule("ACM"),
            InspectionReservationStartsAtRangeRule(32, 1000),
        ),
    )
}
