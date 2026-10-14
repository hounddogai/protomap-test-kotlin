package com.acme.transport.linehaul.tender.reservation

/** A rule a global tender reservation must pass. Check returns its problems, or none. */
interface GlobalTenderReservationRule {
    fun check(item: GlobalTenderReservation): MutableList<String>
}

data class GlobalTenderReservationNameLengthRule(var maxLength: Int) : GlobalTenderReservationRule {
    override fun check(item: GlobalTenderReservation): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class GlobalTenderReservationDurationMinutesRangeRule(
    var min: Int,
    var max: Int,
) : GlobalTenderReservationRule {
    override fun check(item: GlobalTenderReservation): MutableList<String> {
        val value = item.durationMinutes
        if (value < min || value > max) {
            return mutableListOf<String>("duration minutes $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class GlobalTenderReservationVersionRangeRule(var min: Int, var max: Int) : GlobalTenderReservationRule {
    override fun check(item: GlobalTenderReservation): MutableList<String> {
        val value = item.version
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("version $value is below $min")
        }
        if (value > max) {
            problems.add("version $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a global tender reservation. */
data class GlobalTenderReservationValidator(var rules: MutableList<GlobalTenderReservationRule>) {
    fun validate(item: GlobalTenderReservation): MutableList<String> {
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

    fun requireValid(item: GlobalTenderReservation) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<GlobalTenderReservation>): MutableMap<String, MutableList<String>> {
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

fun defaultGlobalTenderReservationValidator(): GlobalTenderReservationValidator {
    return GlobalTenderReservationValidator(
        mutableListOf<GlobalTenderReservationRule>(
            GlobalTenderReservationNameLengthRule(3600),
            GlobalTenderReservationDurationMinutesRangeRule(32, 120),
            GlobalTenderReservationVersionRangeRule(64, 86400),
        ),
    )
}
