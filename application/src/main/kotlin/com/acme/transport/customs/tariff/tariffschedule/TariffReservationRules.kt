package com.acme.transport.customs.tariff.tariffschedule

/** A rule a tariff reservation must pass. Check returns its problems, or none. */
interface TariffReservationRule {
    fun check(item: TariffReservation): MutableList<String>
}

data class TariffReservationEtaMinutesRangeRule(var min: Int, var max: Int) : TariffReservationRule {
    override fun check(item: TariffReservation): MutableList<String> {
        val value = item.etaMinutes
        if (value < min || value > max) {
            return mutableListOf<String>("eta minutes $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class TariffReservationOdometerRangeRule(var min: Int, var max: Int) : TariffReservationRule {
    override fun check(item: TariffReservation): MutableList<String> {
        val value = item.odometer
        if (value < min || value > max) {
            return mutableListOf<String>("odometer $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class TariffReservationNameLengthRule(var maxLength: Int) : TariffReservationRule {
    override fun check(item: TariffReservation): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TariffReservationExpiresAtRangeRule(var min: Int, var max: Int) : TariffReservationRule {
    override fun check(item: TariffReservation): MutableList<String> {
        val value = item.expiresAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("expires at $value is below $min")
        }
        if (value > max) {
            problems.add("expires at $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a tariff reservation. */
data class TariffReservationValidator(var rules: MutableList<TariffReservationRule>) {
    fun validate(item: TariffReservation): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: TariffReservation) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TariffReservation>): MutableMap<String, MutableList<String>> {
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

fun defaultTariffReservationValidator(): TariffReservationValidator {
    return TariffReservationValidator(
        mutableListOf<TariffReservationRule>(
            TariffReservationEtaMinutesRangeRule(120, 120),
            TariffReservationOdometerRangeRule(120, 3600),
            TariffReservationNameLengthRule(80),
            TariffReservationExpiresAtRangeRule(64, 255),
        ),
    )
}
