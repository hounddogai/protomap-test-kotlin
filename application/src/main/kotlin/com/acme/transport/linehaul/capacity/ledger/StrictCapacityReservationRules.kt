package com.acme.transport.linehaul.capacity.ledger

import com.acme.transport.kit.text.normalize

/** A rule a strict capacity reservation must pass. Check returns its problems, or none. */
interface StrictCapacityReservationRule {
    fun check(item: StrictCapacityReservation): MutableList<String>
}

data class StrictCapacityReservationRequiredNameRule(var message: String) : StrictCapacityReservationRule {
    override fun check(item: StrictCapacityReservation): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class StrictCapacityReservationStopCountRangeRule(
    var min: Int,
    var max: Int,
) : StrictCapacityReservationRule {
    override fun check(item: StrictCapacityReservation): MutableList<String> {
        val value = item.stopCount
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("stop count $value is below $min")
        }
        if (value > max) {
            problems.add("stop count $value is above $max")
        }
        return problems
    }
}

data class StrictCapacityReservationRequiredCodeRule(var message: String) : StrictCapacityReservationRule {
    override fun check(item: StrictCapacityReservation): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class StrictCapacityReservationNameLengthRule(var maxLength: Int) : StrictCapacityReservationRule {
    override fun check(item: StrictCapacityReservation): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class StrictCapacityReservationUpdatedAtRangeRule(
    var min: Int,
    var max: Int,
) : StrictCapacityReservationRule {
    override fun check(item: StrictCapacityReservation): MutableList<String> {
        val value = item.updatedAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("updated at $value is below $min")
        }
        if (value > max) {
            problems.add("updated at $value is above $max")
        }
        return problems
    }
}

data class StrictCapacityReservationAllowedNameRule(
    var allowed: MutableList<String>,
) : StrictCapacityReservationRule {
    override fun check(item: StrictCapacityReservation): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.name))) {
            return mutableListOf<String>("name ${item.name} is blocked")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a strict capacity reservation. */
data class StrictCapacityReservationValidator(var rules: MutableList<StrictCapacityReservationRule>) {
    fun validate(item: StrictCapacityReservation): MutableList<String> {
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

    fun requireValid(item: StrictCapacityReservation) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<StrictCapacityReservation>): MutableMap<String, MutableList<String>> {
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

fun defaultStrictCapacityReservationValidator(): StrictCapacityReservationValidator {
    return StrictCapacityReservationValidator(
        mutableListOf<StrictCapacityReservationRule>(
            StrictCapacityReservationRequiredNameRule("strict capacity reservation needs an entry"),
            StrictCapacityReservationStopCountRangeRule(80, 3600),
            StrictCapacityReservationRequiredCodeRule("strict capacity reservation needs this field"),
            StrictCapacityReservationNameLengthRule(255),
            StrictCapacityReservationUpdatedAtRangeRule(16, 3600),
            StrictCapacityReservationAllowedNameRule(mutableListOf<String>("primary", "primary", "rolling")),
        ),
    )
}
