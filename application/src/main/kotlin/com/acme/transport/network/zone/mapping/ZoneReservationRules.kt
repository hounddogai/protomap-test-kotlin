package com.acme.transport.network.zone.mapping

/** A rule a zone reservation must pass. Check returns its problems, or none. */
interface ZoneReservationRule {
    fun check(item: ZoneReservation): MutableList<String>
}

data class ZoneReservationStopCountRangeRule(var min: Int, var max: Int) : ZoneReservationRule {
    override fun check(item: ZoneReservation): MutableList<String> {
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

data class ZoneReservationCodePrefixRule(var prefix: String) : ZoneReservationRule {
    override fun check(item: ZoneReservation): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class ZoneReservationCodeLengthRule(var maxLength: Int) : ZoneReservationRule {
    override fun check(item: ZoneReservation): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a zone reservation. */
data class ZoneReservationValidator(var rules: MutableList<ZoneReservationRule>) {
    fun validate(item: ZoneReservation): MutableList<String> {
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

    fun requireValid(item: ZoneReservation) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ZoneReservation>): MutableMap<String, MutableList<String>> {
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

fun defaultZoneReservationValidator(): ZoneReservationValidator {
    return ZoneReservationValidator(
        mutableListOf<ZoneReservationRule>(
            ZoneReservationStopCountRangeRule(16, 255),
            ZoneReservationCodePrefixRule("OPS"),
            ZoneReservationCodeLengthRule(120),
        ),
    )
}
