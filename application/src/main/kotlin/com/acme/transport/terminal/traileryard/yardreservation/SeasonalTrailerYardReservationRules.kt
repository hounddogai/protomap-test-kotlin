package com.acme.transport.terminal.traileryard.yardreservation

import com.acme.transport.kit.text.normalize

/** A rule a seasonal trailer yard reservation must pass. Check returns its problems, or none. */
interface SeasonalTrailerYardReservationRule {
    fun check(item: SeasonalTrailerYardReservation): MutableList<String>
}

data class SeasonalTrailerYardReservationActiveGuardRule(var limit: Int) : SeasonalTrailerYardReservationRule {
    override fun check(item: SeasonalTrailerYardReservation): MutableList<String> {
        if (item.active && item.createdAt > limit) {
            return mutableListOf<String>("created at ${item.createdAt} exceeds $limit while active")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalTrailerYardReservationCreatedAtRangeRule(
    var min: Int,
    var max: Int,
) : SeasonalTrailerYardReservationRule {
    override fun check(item: SeasonalTrailerYardReservation): MutableList<String> {
        val value = item.createdAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("created at $value is below $min")
        }
        if (value > max) {
            problems.add("created at $value is above $max")
        }
        return problems
    }
}

data class SeasonalTrailerYardReservationAllowedLabelRule(
    var allowed: MutableList<String>,
) : SeasonalTrailerYardReservationRule {
    override fun check(item: SeasonalTrailerYardReservation): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.label))) {
            return mutableListOf<String>("label ${item.label} is out of range")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalTrailerYardReservationDepotLengthRule(
    var maxLength: Int,
) : SeasonalTrailerYardReservationRule {
    override fun check(item: SeasonalTrailerYardReservation): MutableList<String> {
        val size = item.depot.length
        if (size > maxLength) {
            return mutableListOf<String>("depot is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalTrailerYardReservationRequiredCodeRule(
    var message: String,
) : SeasonalTrailerYardReservationRule {
    override fun check(item: SeasonalTrailerYardReservation): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class SeasonalTrailerYardReservationRequiredDepotRule(
    var message: String,
) : SeasonalTrailerYardReservationRule {
    override fun check(item: SeasonalTrailerYardReservation): MutableList<String> {
        if (item.depot.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a seasonal trailer yard reservation. */
data class SeasonalTrailerYardReservationValidator(var rules: MutableList<SeasonalTrailerYardReservationRule>) {
    fun validate(item: SeasonalTrailerYardReservation): MutableList<String> {
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

    fun requireValid(item: SeasonalTrailerYardReservation) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<SeasonalTrailerYardReservation>): MutableMap<String, MutableList<String>> {
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

fun defaultSeasonalTrailerYardReservationValidator(): SeasonalTrailerYardReservationValidator {
    return SeasonalTrailerYardReservationValidator(
        mutableListOf<SeasonalTrailerYardReservationRule>(
            SeasonalTrailerYardReservationActiveGuardRule(3600),
            SeasonalTrailerYardReservationCreatedAtRangeRule(255, 86400),
            SeasonalTrailerYardReservationAllowedLabelRule(mutableListOf<String>()),
            SeasonalTrailerYardReservationDepotLengthRule(86400),
            SeasonalTrailerYardReservationRequiredCodeRule("seasonal trailer yard reservation needs a value"),
            SeasonalTrailerYardReservationRequiredDepotRule(
                "seasonal trailer yard reservation needs this field",
            ),
        ),
    )
}
