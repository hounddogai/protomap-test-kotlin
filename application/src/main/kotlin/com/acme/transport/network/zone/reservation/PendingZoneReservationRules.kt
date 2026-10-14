package com.acme.transport.network.zone.reservation

import com.acme.transport.kit.text.normalize

/** A rule a pending zone reservation must pass. Check returns its problems, or none. */
interface PendingZoneReservationRule {
    fun check(item: PendingZoneReservation): MutableList<String>
}

data class PendingZoneReservationAllowedLabelRule(
    var allowed: MutableList<String>,
) : PendingZoneReservationRule {
    override fun check(item: PendingZoneReservation): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.label))) {
            return mutableListOf<String>("label ${item.label} is not allowed")
        }
        return mutableListOf<String>()
    }
}

data class PendingZoneReservationRequiredChannelRule(var message: String) : PendingZoneReservationRule {
    override fun check(item: PendingZoneReservation): MutableList<String> {
        if (item.channel.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PendingZoneReservationArchivedGuardRule(var limit: Int) : PendingZoneReservationRule {
    override fun check(item: PendingZoneReservation): MutableList<String> {
        if (item.archived && item.attempts > limit) {
            return mutableListOf<String>("attempts ${item.attempts} exceeds $limit while archived")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a pending zone reservation. */
data class PendingZoneReservationValidator(var rules: MutableList<PendingZoneReservationRule>) {
    fun validate(item: PendingZoneReservation): MutableList<String> {
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

    fun requireValid(item: PendingZoneReservation) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PendingZoneReservation>): MutableMap<String, MutableList<String>> {
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

fun defaultPendingZoneReservationValidator(): PendingZoneReservationValidator {
    return PendingZoneReservationValidator(
        mutableListOf<PendingZoneReservationRule>(
            PendingZoneReservationAllowedLabelRule(mutableListOf<String>("primary", "batched")),
            PendingZoneReservationRequiredChannelRule("pending zone reservation needs this field"),
            PendingZoneReservationArchivedGuardRule(1000),
        ),
    )
}
