package com.acme.transport.fleetops.leasereturn.proposal

/** A rule a lease return reservation must pass. Check returns its problems, or none. */
interface LeaseReturnReservationRule {
    fun check(item: LeaseReturnReservation): MutableList<String>
}

data class LeaseReturnReservationStopCountRangeRule(var min: Int, var max: Int) : LeaseReturnReservationRule {
    override fun check(item: LeaseReturnReservation): MutableList<String> {
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

data class LeaseReturnReservationRequiredDepotRule(var message: String) : LeaseReturnReservationRule {
    override fun check(item: LeaseReturnReservation): MutableList<String> {
        if (item.depot.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LeaseReturnReservationAttemptsRangeRule(var min: Int, var max: Int) : LeaseReturnReservationRule {
    override fun check(item: LeaseReturnReservation): MutableList<String> {
        val value = item.attempts
        if (value < min || value > max) {
            return mutableListOf<String>("attempts $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class LeaseReturnReservationLoadKgRangeRule(
    var min: Double,
    var max: Double,
) : LeaseReturnReservationRule {
    override fun check(item: LeaseReturnReservation): MutableList<String> {
        val value = item.loadKg
        if (value < min || value > max) {
            return mutableListOf<String>("load kg $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a lease return reservation. */
data class LeaseReturnReservationValidator(var rules: MutableList<LeaseReturnReservationRule>) {
    fun validate(item: LeaseReturnReservation): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: LeaseReturnReservation) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LeaseReturnReservation>): MutableMap<String, MutableList<String>> {
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

fun defaultLeaseReturnReservationValidator(): LeaseReturnReservationValidator {
    return LeaseReturnReservationValidator(
        mutableListOf<LeaseReturnReservationRule>(
            LeaseReturnReservationStopCountRangeRule(16, 255),
            LeaseReturnReservationRequiredDepotRule("lease return reservation needs a value"),
            LeaseReturnReservationAttemptsRangeRule(120, 120),
            LeaseReturnReservationLoadKgRangeRule(3.5, 215.6),
        ),
    )
}
