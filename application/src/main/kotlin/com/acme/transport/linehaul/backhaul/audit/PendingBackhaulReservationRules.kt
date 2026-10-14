package com.acme.transport.linehaul.backhaul.audit

import com.acme.transport.kit.text.normalize

/** A rule a pending backhaul reservation must pass. Check returns its problems, or none. */
interface PendingBackhaulReservationRule {
    fun check(item: PendingBackhaulReservation): MutableList<String>
}

data class PendingBackhaulReservationRequiredDriverIdRule(
    var message: String,
) : PendingBackhaulReservationRule {
    override fun check(item: PendingBackhaulReservation): MutableList<String> {
        if (item.driverId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PendingBackhaulReservationPhoneLengthRule(var maxLength: Int) : PendingBackhaulReservationRule {
    override fun check(item: PendingBackhaulReservation): MutableList<String> {
        val size = item.phone.length
        if (size > maxLength) {
            return mutableListOf<String>("phone is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PendingBackhaulReservationAllowedOwnerIdRule(
    var allowed: MutableList<String>,
) : PendingBackhaulReservationRule {
    override fun check(item: PendingBackhaulReservation): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.ownerId))) {
            return mutableListOf<String>("owner id ${item.ownerId} is unknown")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a pending backhaul reservation. */
data class PendingBackhaulReservationValidator(var rules: MutableList<PendingBackhaulReservationRule>) {
    fun validate(item: PendingBackhaulReservation): MutableList<String> {
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

    fun requireValid(item: PendingBackhaulReservation) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PendingBackhaulReservation>): MutableMap<String, MutableList<String>> {
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

fun defaultPendingBackhaulReservationValidator(): PendingBackhaulReservationValidator {
    return PendingBackhaulReservationValidator(
        mutableListOf<PendingBackhaulReservationRule>(
            PendingBackhaulReservationRequiredDriverIdRule("pending backhaul reservation needs a value"),
            PendingBackhaulReservationPhoneLengthRule(86400),
            PendingBackhaulReservationAllowedOwnerIdRule(mutableListOf<String>()),
        ),
    )
}
