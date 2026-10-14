package com.acme.transport.linehaul.relay.relayassignment

/** A rule a manual relay reservation must pass. Check returns its problems, or none. */
interface ManualRelayReservationRule {
    fun check(item: ManualRelayReservation): MutableList<String>
}

data class ManualRelayReservationCodePrefixRule(var prefix: String) : ManualRelayReservationRule {
    override fun check(item: ManualRelayReservation): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class ManualRelayReservationDriverIdLengthRule(var maxLength: Int) : ManualRelayReservationRule {
    override fun check(item: ManualRelayReservation): MutableList<String> {
        val size = item.driverId.length
        if (size > maxLength) {
            return mutableListOf<String>("driver id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ManualRelayReservationShiftCodeLengthRule(var maxLength: Int) : ManualRelayReservationRule {
    override fun check(item: ManualRelayReservation): MutableList<String> {
        val size = item.shiftCode.length
        if (size > maxLength) {
            return mutableListOf<String>("shift code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ManualRelayReservationStopCountRangeRule(var min: Int, var max: Int) : ManualRelayReservationRule {
    override fun check(item: ManualRelayReservation): MutableList<String> {
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

/** Runs every rule of a manual relay reservation. */
data class ManualRelayReservationValidator(var rules: MutableList<ManualRelayReservationRule>) {
    fun validate(item: ManualRelayReservation): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ManualRelayReservation) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ManualRelayReservation>): MutableMap<String, MutableList<String>> {
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

fun defaultManualRelayReservationValidator(): ManualRelayReservationValidator {
    return ManualRelayReservationValidator(
        mutableListOf<ManualRelayReservationRule>(
            ManualRelayReservationCodePrefixRule("REF"),
            ManualRelayReservationDriverIdLengthRule(3600),
            ManualRelayReservationShiftCodeLengthRule(86400),
            ManualRelayReservationStopCountRangeRule(16, 120),
        ),
    )
}
