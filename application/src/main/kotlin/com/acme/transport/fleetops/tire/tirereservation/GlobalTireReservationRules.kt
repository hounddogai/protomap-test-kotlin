package com.acme.transport.fleetops.tire.tirereservation

import com.acme.transport.kit.text.normalize

/** A rule a global tire reservation must pass. Check returns its problems, or none. */
interface GlobalTireReservationRule {
    fun check(item: GlobalTireReservation): MutableList<String>
}

data class GlobalTireReservationArchivedGuardRule(var limit: Int) : GlobalTireReservationRule {
    override fun check(item: GlobalTireReservation): MutableList<String> {
        if (item.archived && item.etaMinutes > limit) {
            return mutableListOf<String>("eta minutes ${item.etaMinutes} exceeds $limit while archived")
        }
        return mutableListOf<String>()
    }
}

data class GlobalTireReservationCodeLengthRule(var maxLength: Int) : GlobalTireReservationRule {
    override fun check(item: GlobalTireReservation): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class GlobalTireReservationAllowedCarrierRule(
    var allowed: MutableList<String>,
) : GlobalTireReservationRule {
    override fun check(item: GlobalTireReservation): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.carrier))) {
            return mutableListOf<String>("carrier ${item.carrier} is unknown")
        }
        return mutableListOf<String>()
    }
}

data class GlobalTireReservationCodePrefixRule(var prefix: String) : GlobalTireReservationRule {
    override fun check(item: GlobalTireReservation): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class GlobalTireReservationRetriesRangeRule(var min: Int, var max: Int) : GlobalTireReservationRule {
    override fun check(item: GlobalTireReservation): MutableList<String> {
        val value = item.retries
        if (value < min || value > max) {
            return mutableListOf<String>("retries $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a global tire reservation. */
data class GlobalTireReservationValidator(var rules: MutableList<GlobalTireReservationRule>) {
    fun validate(item: GlobalTireReservation): MutableList<String> {
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

    fun requireValid(item: GlobalTireReservation) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<GlobalTireReservation>): MutableMap<String, MutableList<String>> {
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

fun defaultGlobalTireReservationValidator(): GlobalTireReservationValidator {
    return GlobalTireReservationValidator(
        mutableListOf<GlobalTireReservationRule>(
            GlobalTireReservationArchivedGuardRule(1000),
            GlobalTireReservationCodeLengthRule(255),
            GlobalTireReservationAllowedCarrierRule(mutableListOf<String>("legacy", "regional")),
            GlobalTireReservationCodePrefixRule("X"),
            GlobalTireReservationRetriesRangeRule(80, 120),
        ),
    )
}
