package com.acme.transport.network.node.policy

/** A rule a rolling node reservation must pass. Check returns its problems, or none. */
interface RollingNodeReservationRule {
    fun check(item: RollingNodeReservation): MutableList<String>
}

data class RollingNodeReservationNotesLengthRule(var maxLength: Int) : RollingNodeReservationRule {
    override fun check(item: RollingNodeReservation): MutableList<String> {
        val size = item.notes.length
        if (size > maxLength) {
            return mutableListOf<String>("notes is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RollingNodeReservationEndsAtRangeRule(var min: Int, var max: Int) : RollingNodeReservationRule {
    override fun check(item: RollingNodeReservation): MutableList<String> {
        val value = item.endsAt
        if (value < min || value > max) {
            return mutableListOf<String>("ends at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class RollingNodeReservationAttemptsRangeRule(var min: Int, var max: Int) : RollingNodeReservationRule {
    override fun check(item: RollingNodeReservation): MutableList<String> {
        val value = item.attempts
        if (value < min || value > max) {
            return mutableListOf<String>("attempts $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a rolling node reservation. */
data class RollingNodeReservationValidator(var rules: MutableList<RollingNodeReservationRule>) {
    fun validate(item: RollingNodeReservation): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RollingNodeReservation) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RollingNodeReservation>): MutableMap<String, MutableList<String>> {
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

fun defaultRollingNodeReservationValidator(): RollingNodeReservationValidator {
    return RollingNodeReservationValidator(
        mutableListOf<RollingNodeReservationRule>(
            RollingNodeReservationNotesLengthRule(255),
            RollingNodeReservationEndsAtRangeRule(64, 64),
            RollingNodeReservationAttemptsRangeRule(32, 255),
        ),
    )
}
