package com.acme.transport.network.zone.group

import com.acme.transport.kit.text.normalize

/** A rule a capped zone reservation must pass. Check returns its problems, or none. */
interface CappedZoneReservationRule {
    fun check(item: CappedZoneReservation): MutableList<String>
}

data class CappedZoneReservationDurationMinutesRangeRule(
    var min: Int,
    var max: Int,
) : CappedZoneReservationRule {
    override fun check(item: CappedZoneReservation): MutableList<String> {
        val value = item.durationMinutes
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("duration minutes $value is below $min")
        }
        if (value > max) {
            problems.add("duration minutes $value is above $max")
        }
        return problems
    }
}

data class CappedZoneReservationAllowedShiftCodeRule(
    var allowed: MutableList<String>,
) : CappedZoneReservationRule {
    override fun check(item: CappedZoneReservation): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.shiftCode))) {
            return mutableListOf<String>("shift code ${item.shiftCode} is above maximum")
        }
        return mutableListOf<String>()
    }
}

data class CappedZoneReservationRequiredLocaleRule(var message: String) : CappedZoneReservationRule {
    override fun check(item: CappedZoneReservation): MutableList<String> {
        if (item.locale.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CappedZoneReservationCodeLengthRule(var maxLength: Int) : CappedZoneReservationRule {
    override fun check(item: CappedZoneReservation): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a capped zone reservation. */
data class CappedZoneReservationValidator(var rules: MutableList<CappedZoneReservationRule>) {
    fun validate(item: CappedZoneReservation): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: CappedZoneReservation) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CappedZoneReservation>): MutableMap<String, MutableList<String>> {
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

fun defaultCappedZoneReservationValidator(): CappedZoneReservationValidator {
    return CappedZoneReservationValidator(
        mutableListOf<CappedZoneReservationRule>(
            CappedZoneReservationDurationMinutesRangeRule(32, 80),
            CappedZoneReservationAllowedShiftCodeRule(mutableListOf<String>()),
            CappedZoneReservationRequiredLocaleRule("capped zone reservation needs an entry"),
            CappedZoneReservationCodeLengthRule(120),
        ),
    )
}
