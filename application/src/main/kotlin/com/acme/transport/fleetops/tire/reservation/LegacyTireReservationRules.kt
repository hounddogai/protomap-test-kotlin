package com.acme.transport.fleetops.tire.reservation

/** A rule a legacy tire reservation must pass. Check returns its problems, or none. */
interface LegacyTireReservationRule {
    fun check(item: LegacyTireReservation): MutableList<String>
}

data class LegacyTireReservationRequiredRouteCodeRule(var message: String) : LegacyTireReservationRule {
    override fun check(item: LegacyTireReservation): MutableList<String> {
        if (item.routeCode.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LegacyTireReservationRequiredCodeRule(var message: String) : LegacyTireReservationRule {
    override fun check(item: LegacyTireReservation): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LegacyTireReservationEtaMinutesRangeRule(var min: Int, var max: Int) : LegacyTireReservationRule {
    override fun check(item: LegacyTireReservation): MutableList<String> {
        val value = item.etaMinutes
        if (value < min || value > max) {
            return mutableListOf<String>("eta minutes $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class LegacyTireReservationRequiredNameRule(var message: String) : LegacyTireReservationRule {
    override fun check(item: LegacyTireReservation): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LegacyTireReservationTrailerIdLengthRule(var maxLength: Int) : LegacyTireReservationRule {
    override fun check(item: LegacyTireReservation): MutableList<String> {
        val size = item.trailerId.length
        if (size > maxLength) {
            return mutableListOf<String>("trailer id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a legacy tire reservation. */
data class LegacyTireReservationValidator(var rules: MutableList<LegacyTireReservationRule>) {
    fun validate(item: LegacyTireReservation): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: LegacyTireReservation) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LegacyTireReservation>): MutableMap<String, MutableList<String>> {
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

fun defaultLegacyTireReservationValidator(): LegacyTireReservationValidator {
    return LegacyTireReservationValidator(
        mutableListOf<LegacyTireReservationRule>(
            LegacyTireReservationRequiredRouteCodeRule("legacy tire reservation needs a value"),
            LegacyTireReservationRequiredCodeRule("legacy tire reservation needs an entry"),
            LegacyTireReservationEtaMinutesRangeRule(16, 86400),
            LegacyTireReservationRequiredNameRule("legacy tire reservation needs an entry"),
            LegacyTireReservationTrailerIdLengthRule(32),
        ),
    )
}
