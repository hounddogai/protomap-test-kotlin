package com.acme.transport.network.zone.schedule

import com.acme.transport.kit.text.normalize

/** A rule a scheduled zone reservation must pass. Check returns its problems, or none. */
interface ScheduledZoneReservationRule {
    fun check(item: ScheduledZoneReservation): MutableList<String>
}

data class ScheduledZoneReservationLoadKgRangeRule(
    var min: Double,
    var max: Double,
) : ScheduledZoneReservationRule {
    override fun check(item: ScheduledZoneReservation): MutableList<String> {
        val value = item.loadKg
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("load kg $value is below $min")
        }
        if (value > max) {
            problems.add("load kg $value is above $max")
        }
        return problems
    }
}

data class ScheduledZoneReservationRequiredCarrierRule(var message: String) : ScheduledZoneReservationRule {
    override fun check(item: ScheduledZoneReservation): MutableList<String> {
        if (item.carrier.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ScheduledZoneReservationCodePrefixRule(var prefix: String) : ScheduledZoneReservationRule {
    override fun check(item: ScheduledZoneReservation): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledZoneReservationRequiredCodeRule(var message: String) : ScheduledZoneReservationRule {
    override fun check(item: ScheduledZoneReservation): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ScheduledZoneReservationRequiredLabelRule(var message: String) : ScheduledZoneReservationRule {
    override fun check(item: ScheduledZoneReservation): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ScheduledZoneReservationAllowedLabelRule(
    var allowed: MutableList<String>,
) : ScheduledZoneReservationRule {
    override fun check(item: ScheduledZoneReservation): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.label))) {
            return mutableListOf<String>("label ${item.label} is blocked")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a scheduled zone reservation. */
data class ScheduledZoneReservationValidator(var rules: MutableList<ScheduledZoneReservationRule>) {
    fun validate(item: ScheduledZoneReservation): MutableList<String> {
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

    fun requireValid(item: ScheduledZoneReservation) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ScheduledZoneReservation>): MutableMap<String, MutableList<String>> {
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

fun defaultScheduledZoneReservationValidator(): ScheduledZoneReservationValidator {
    return ScheduledZoneReservationValidator(
        mutableListOf<ScheduledZoneReservationRule>(
            ScheduledZoneReservationLoadKgRangeRule(4.2, 3901.0),
            ScheduledZoneReservationRequiredCarrierRule("scheduled zone reservation needs this field"),
            ScheduledZoneReservationCodePrefixRule("ACM"),
            ScheduledZoneReservationRequiredCodeRule("scheduled zone reservation needs this field"),
            ScheduledZoneReservationRequiredLabelRule("scheduled zone reservation needs a value"),
            ScheduledZoneReservationAllowedLabelRule(mutableListOf<String>("rolling", "regional")),
        ),
    )
}
