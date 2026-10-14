package com.acme.transport.linehaul.relay.notice

/** A rule a relay reservation must pass. Check returns its problems, or none. */
interface RelayReservationRule {
    fun check(item: RelayReservation): MutableList<String>
}

data class RelayReservationCodeLengthRule(var maxLength: Int) : RelayReservationRule {
    override fun check(item: RelayReservation): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RelayReservationQuantityRangeRule(var min: Int, var max: Int) : RelayReservationRule {
    override fun check(item: RelayReservation): MutableList<String> {
        val value = item.quantity
        if (value < min || value > max) {
            return mutableListOf<String>("quantity $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class RelayReservationUnitLengthRule(var maxLength: Int) : RelayReservationRule {
    override fun check(item: RelayReservation): MutableList<String> {
        val size = item.unit.length
        if (size > maxLength) {
            return mutableListOf<String>("unit is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a relay reservation. */
data class RelayReservationValidator(var rules: MutableList<RelayReservationRule>) {
    fun validate(item: RelayReservation): MutableList<String> {
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

    fun requireValid(item: RelayReservation) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RelayReservation>): MutableMap<String, MutableList<String>> {
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

fun defaultRelayReservationValidator(): RelayReservationValidator {
    return RelayReservationValidator(
        mutableListOf<RelayReservationRule>(
            RelayReservationCodeLengthRule(32),
            RelayReservationQuantityRangeRule(80, 1000),
            RelayReservationUnitLengthRule(3600),
        ),
    )
}
