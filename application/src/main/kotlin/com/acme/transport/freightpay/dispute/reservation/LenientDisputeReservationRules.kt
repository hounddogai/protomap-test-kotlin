package com.acme.transport.freightpay.dispute.reservation

import com.acme.transport.kit.text.normalize

/** A rule a lenient dispute reservation must pass. Check returns its problems, or none. */
interface LenientDisputeReservationRule {
    fun check(item: LenientDisputeReservation): MutableList<String>
}

data class LenientDisputeReservationVersionRangeRule(
    var min: Int,
    var max: Int,
) : LenientDisputeReservationRule {
    override fun check(item: LenientDisputeReservation): MutableList<String> {
        val value = item.version
        if (value < min || value > max) {
            return mutableListOf<String>("version $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class LenientDisputeReservationAttemptsRangeRule(
    var min: Int,
    var max: Int,
) : LenientDisputeReservationRule {
    override fun check(item: LenientDisputeReservation): MutableList<String> {
        val value = item.attempts
        if (value < min || value > max) {
            return mutableListOf<String>("attempts $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class LenientDisputeReservationAllowedChannelRule(
    var allowed: MutableList<String>,
) : LenientDisputeReservationRule {
    override fun check(item: LenientDisputeReservation): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.channel))) {
            return mutableListOf<String>("channel ${item.channel} is above maximum")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a lenient dispute reservation. */
data class LenientDisputeReservationValidator(var rules: MutableList<LenientDisputeReservationRule>) {
    fun validate(item: LenientDisputeReservation): MutableList<String> {
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

    fun requireValid(item: LenientDisputeReservation) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LenientDisputeReservation>): MutableMap<String, MutableList<String>> {
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

fun defaultLenientDisputeReservationValidator(): LenientDisputeReservationValidator {
    return LenientDisputeReservationValidator(
        mutableListOf<LenientDisputeReservationRule>(
            LenientDisputeReservationVersionRangeRule(86400, 86400),
            LenientDisputeReservationAttemptsRangeRule(64, 64),
            LenientDisputeReservationAllowedChannelRule(mutableListOf<String>("fallback", "scheduled")),
        ),
    )
}
