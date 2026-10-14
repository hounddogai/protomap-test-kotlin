package com.acme.transport.terminal.traileryard.assignment

/** A rule a trailer yard reservation must pass. Check returns its problems, or none. */
interface TrailerYardReservationRule {
    fun check(item: TrailerYardReservation): MutableList<String>
}

data class TrailerYardReservationTitleLengthRule(var maxLength: Int) : TrailerYardReservationRule {
    override fun check(item: TrailerYardReservation): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TrailerYardReservationStopCountRangeRule(var min: Int, var max: Int) : TrailerYardReservationRule {
    override fun check(item: TrailerYardReservation): MutableList<String> {
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

data class TrailerYardReservationCurrencyLengthRule(var maxLength: Int) : TrailerYardReservationRule {
    override fun check(item: TrailerYardReservation): MutableList<String> {
        val size = item.currency.length
        if (size > maxLength) {
            return mutableListOf<String>("currency is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a trailer yard reservation. */
data class TrailerYardReservationValidator(var rules: MutableList<TrailerYardReservationRule>) {
    fun validate(item: TrailerYardReservation): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: TrailerYardReservation) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TrailerYardReservation>): MutableMap<String, MutableList<String>> {
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

fun defaultTrailerYardReservationValidator(): TrailerYardReservationValidator {
    return TrailerYardReservationValidator(
        mutableListOf<TrailerYardReservationRule>(
            TrailerYardReservationTitleLengthRule(3600),
            TrailerYardReservationStopCountRangeRule(80, 86400),
            TrailerYardReservationCurrencyLengthRule(32),
        ),
    )
}
