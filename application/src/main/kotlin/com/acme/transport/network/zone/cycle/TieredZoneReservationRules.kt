package com.acme.transport.network.zone.cycle

/** A rule a tiered zone reservation must pass. Check returns its problems, or none. */
interface TieredZoneReservationRule {
    fun check(item: TieredZoneReservation): MutableList<String>
}

data class TieredZoneReservationRevisionRangeRule(var min: Int, var max: Int) : TieredZoneReservationRule {
    override fun check(item: TieredZoneReservation): MutableList<String> {
        val value = item.revision
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("revision $value is below $min")
        }
        if (value > max) {
            problems.add("revision $value is above $max")
        }
        return problems
    }
}

data class TieredZoneReservationDescriptionLengthRule(var maxLength: Int) : TieredZoneReservationRule {
    override fun check(item: TieredZoneReservation): MutableList<String> {
        val size = item.description.length
        if (size > maxLength) {
            return mutableListOf<String>("description is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TieredZoneReservationCodeLengthRule(var maxLength: Int) : TieredZoneReservationRule {
    override fun check(item: TieredZoneReservation): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a tiered zone reservation. */
data class TieredZoneReservationValidator(var rules: MutableList<TieredZoneReservationRule>) {
    fun validate(item: TieredZoneReservation): MutableList<String> {
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

    fun requireValid(item: TieredZoneReservation) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TieredZoneReservation>): MutableMap<String, MutableList<String>> {
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

fun defaultTieredZoneReservationValidator(): TieredZoneReservationValidator {
    return TieredZoneReservationValidator(
        mutableListOf<TieredZoneReservationRule>(
            TieredZoneReservationRevisionRangeRule(64, 255),
            TieredZoneReservationDescriptionLengthRule(1000),
            TieredZoneReservationCodeLengthRule(32),
        ),
    )
}
