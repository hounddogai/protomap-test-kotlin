package com.acme.transport.linehaul.backhaul.backhaulreservation

/** A rule a legacy backhaul reservation must pass. Check returns its problems, or none. */
interface LegacyBackhaulReservationRule {
    fun check(item: LegacyBackhaulReservation): MutableList<String>
}

data class LegacyBackhaulReservationNameLengthRule(var maxLength: Int) : LegacyBackhaulReservationRule {
    override fun check(item: LegacyBackhaulReservation): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LegacyBackhaulReservationRequiredVehicleIdRule(var message: String) : LegacyBackhaulReservationRule {
    override fun check(item: LegacyBackhaulReservation): MutableList<String> {
        if (item.vehicleId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LegacyBackhaulReservationAttemptsRangeRule(
    var min: Int,
    var max: Int,
) : LegacyBackhaulReservationRule {
    override fun check(item: LegacyBackhaulReservation): MutableList<String> {
        val value = item.attempts
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("attempts $value is below $min")
        }
        if (value > max) {
            problems.add("attempts $value is above $max")
        }
        return problems
    }
}

data class LegacyBackhaulReservationVehicleIdLengthRule(var maxLength: Int) : LegacyBackhaulReservationRule {
    override fun check(item: LegacyBackhaulReservation): MutableList<String> {
        val size = item.vehicleId.length
        if (size > maxLength) {
            return mutableListOf<String>("vehicle id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a legacy backhaul reservation. */
data class LegacyBackhaulReservationValidator(var rules: MutableList<LegacyBackhaulReservationRule>) {
    fun validate(item: LegacyBackhaulReservation): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: LegacyBackhaulReservation) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LegacyBackhaulReservation>): MutableMap<String, MutableList<String>> {
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

fun defaultLegacyBackhaulReservationValidator(): LegacyBackhaulReservationValidator {
    return LegacyBackhaulReservationValidator(
        mutableListOf<LegacyBackhaulReservationRule>(
            LegacyBackhaulReservationNameLengthRule(1000),
            LegacyBackhaulReservationRequiredVehicleIdRule("legacy backhaul reservation needs an entry"),
            LegacyBackhaulReservationAttemptsRangeRule(16, 64),
            LegacyBackhaulReservationVehicleIdLengthRule(80),
        ),
    )
}
