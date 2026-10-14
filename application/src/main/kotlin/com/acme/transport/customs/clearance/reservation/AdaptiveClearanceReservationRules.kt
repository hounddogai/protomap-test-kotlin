package com.acme.transport.customs.clearance.reservation

import com.acme.transport.kit.text.normalize

/** A rule a adaptive clearance reservation must pass. Check returns its problems, or none. */
interface AdaptiveClearanceReservationRule {
    fun check(item: AdaptiveClearanceReservation): MutableList<String>
}

data class AdaptiveClearanceReservationFuelLitersRangeRule(
    var min: Double,
    var max: Double,
) : AdaptiveClearanceReservationRule {
    override fun check(item: AdaptiveClearanceReservation): MutableList<String> {
        val value = item.fuelLiters
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("fuel liters $value is below $min")
        }
        if (value > max) {
            problems.add("fuel liters $value is above $max")
        }
        return problems
    }
}

data class AdaptiveClearanceReservationAllowedCodeRule(
    var allowed: MutableList<String>,
) : AdaptiveClearanceReservationRule {
    override fun check(item: AdaptiveClearanceReservation): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is duplicated")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveClearanceReservationLabelLengthRule(var maxLength: Int) : AdaptiveClearanceReservationRule {
    override fun check(item: AdaptiveClearanceReservation): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveClearanceReservationRequiredDockRule(
    var message: String,
) : AdaptiveClearanceReservationRule {
    override fun check(item: AdaptiveClearanceReservation): MutableList<String> {
        if (item.dock.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveClearanceReservationRequiredCodeRule(
    var message: String,
) : AdaptiveClearanceReservationRule {
    override fun check(item: AdaptiveClearanceReservation): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveClearanceReservationCodeLengthRule(var maxLength: Int) : AdaptiveClearanceReservationRule {
    override fun check(item: AdaptiveClearanceReservation): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a adaptive clearance reservation. */
data class AdaptiveClearanceReservationValidator(var rules: MutableList<AdaptiveClearanceReservationRule>) {
    fun validate(item: AdaptiveClearanceReservation): MutableList<String> {
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

    fun requireValid(item: AdaptiveClearanceReservation) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AdaptiveClearanceReservation>): MutableMap<String, MutableList<String>> {
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

fun defaultAdaptiveClearanceReservationValidator(): AdaptiveClearanceReservationValidator {
    return AdaptiveClearanceReservationValidator(
        mutableListOf<AdaptiveClearanceReservationRule>(
            AdaptiveClearanceReservationFuelLitersRangeRule(1.4, 3597.4),
            AdaptiveClearanceReservationAllowedCodeRule(mutableListOf<String>("weighted", "adaptive", "tiered")),
            AdaptiveClearanceReservationLabelLengthRule(120),
            AdaptiveClearanceReservationRequiredDockRule("adaptive clearance reservation needs this field"),
            AdaptiveClearanceReservationRequiredCodeRule("adaptive clearance reservation needs a value"),
            AdaptiveClearanceReservationCodeLengthRule(86400),
        ),
    )
}
