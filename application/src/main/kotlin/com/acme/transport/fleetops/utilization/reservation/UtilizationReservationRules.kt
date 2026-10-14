package com.acme.transport.fleetops.utilization.reservation

/** A rule a utilization reservation must pass. Check returns its problems, or none. */
interface UtilizationReservationRule {
    fun check(item: UtilizationReservation): MutableList<String>
}

data class UtilizationReservationExpiresAtRangeRule(var min: Int, var max: Int) : UtilizationReservationRule {
    override fun check(item: UtilizationReservation): MutableList<String> {
        val value = item.expiresAt
        if (value < min || value > max) {
            return mutableListOf<String>("expires at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class UtilizationReservationCodePrefixRule(var prefix: String) : UtilizationReservationRule {
    override fun check(item: UtilizationReservation): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class UtilizationReservationRequiredTitleRule(var message: String) : UtilizationReservationRule {
    override fun check(item: UtilizationReservation): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class UtilizationReservationRequiredCodeRule(var message: String) : UtilizationReservationRule {
    override fun check(item: UtilizationReservation): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a utilization reservation. */
data class UtilizationReservationValidator(var rules: MutableList<UtilizationReservationRule>) {
    fun validate(item: UtilizationReservation): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: UtilizationReservation) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<UtilizationReservation>): MutableMap<String, MutableList<String>> {
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

fun defaultUtilizationReservationValidator(): UtilizationReservationValidator {
    return UtilizationReservationValidator(
        mutableListOf<UtilizationReservationRule>(
            UtilizationReservationExpiresAtRangeRule(16, 3600),
            UtilizationReservationCodePrefixRule("ACM"),
            UtilizationReservationRequiredTitleRule("utilization reservation needs an entry"),
            UtilizationReservationRequiredCodeRule("utilization reservation needs an entry"),
        ),
    )
}
