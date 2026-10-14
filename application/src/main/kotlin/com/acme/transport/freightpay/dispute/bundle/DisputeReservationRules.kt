package com.acme.transport.freightpay.dispute.bundle

import com.acme.transport.kit.text.normalize

/** A rule a dispute reservation must pass. Check returns its problems, or none. */
interface DisputeReservationRule {
    fun check(item: DisputeReservation): MutableList<String>
}

data class DisputeReservationCodeLengthRule(var maxLength: Int) : DisputeReservationRule {
    override fun check(item: DisputeReservation): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DisputeReservationAllowedRouteCodeRule(var allowed: MutableList<String>) : DisputeReservationRule {
    override fun check(item: DisputeReservation): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.routeCode))) {
            return mutableListOf<String>("route code ${item.routeCode} is missing")
        }
        return mutableListOf<String>()
    }
}

data class DisputeReservationDiscountRangeRule(var min: Double, var max: Double) : DisputeReservationRule {
    override fun check(item: DisputeReservation): MutableList<String> {
        val value = item.discount
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("discount $value is below $min")
        }
        if (value > max) {
            problems.add("discount $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a dispute reservation. */
data class DisputeReservationValidator(var rules: MutableList<DisputeReservationRule>) {
    fun validate(item: DisputeReservation): MutableList<String> {
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

    fun requireValid(item: DisputeReservation) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DisputeReservation>): MutableMap<String, MutableList<String>> {
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

fun defaultDisputeReservationValidator(): DisputeReservationValidator {
    return DisputeReservationValidator(
        mutableListOf<DisputeReservationRule>(
            DisputeReservationCodeLengthRule(255),
            DisputeReservationAllowedRouteCodeRule(mutableListOf<String>("adaptive")),
            DisputeReservationDiscountRangeRule(6.3, 2841.2),
        ),
    )
}
