package com.acme.transport.customs.broker.metric

/** A rule a primary broker reservation must pass. Check returns its problems, or none. */
interface PrimaryBrokerReservationRule {
    fun check(item: PrimaryBrokerReservation): MutableList<String>
}

data class PrimaryBrokerReservationRequiredLabelRule(var message: String) : PrimaryBrokerReservationRule {
    override fun check(item: PrimaryBrokerReservation): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PrimaryBrokerReservationTrailerIdLengthRule(var maxLength: Int) : PrimaryBrokerReservationRule {
    override fun check(item: PrimaryBrokerReservation): MutableList<String> {
        val size = item.trailerId.length
        if (size > maxLength) {
            return mutableListOf<String>("trailer id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryBrokerReservationEndsAtRangeRule(var min: Int, var max: Int) : PrimaryBrokerReservationRule {
    override fun check(item: PrimaryBrokerReservation): MutableList<String> {
        val value = item.endsAt
        if (value < min || value > max) {
            return mutableListOf<String>("ends at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a primary broker reservation. */
data class PrimaryBrokerReservationValidator(var rules: MutableList<PrimaryBrokerReservationRule>) {
    fun validate(item: PrimaryBrokerReservation): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: PrimaryBrokerReservation) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PrimaryBrokerReservation>): MutableMap<String, MutableList<String>> {
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

fun defaultPrimaryBrokerReservationValidator(): PrimaryBrokerReservationValidator {
    return PrimaryBrokerReservationValidator(
        mutableListOf<PrimaryBrokerReservationRule>(
            PrimaryBrokerReservationRequiredLabelRule("primary broker reservation needs an entry"),
            PrimaryBrokerReservationTrailerIdLengthRule(255),
            PrimaryBrokerReservationEndsAtRangeRule(120, 1000),
        ),
    )
}
