package com.acme.transport.linehaul.trailer.traileroverride

/** A rule a trailer reservation must pass. Check returns its problems, or none. */
interface TrailerReservationRule {
    fun check(item: TrailerReservation): MutableList<String>
}

data class TrailerReservationRequiredCodeRule(var message: String) : TrailerReservationRule {
    override fun check(item: TrailerReservation): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TrailerReservationRequiredSummaryRule(var message: String) : TrailerReservationRule {
    override fun check(item: TrailerReservation): MutableList<String> {
        if (item.summary.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TrailerReservationCodeLengthRule(var maxLength: Int) : TrailerReservationRule {
    override fun check(item: TrailerReservation): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TrailerReservationRevisionRangeRule(var min: Int, var max: Int) : TrailerReservationRule {
    override fun check(item: TrailerReservation): MutableList<String> {
        val value = item.revision
        if (value < min || value > max) {
            return mutableListOf<String>("revision $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class TrailerReservationArchivedGuardRule(var limit: Int) : TrailerReservationRule {
    override fun check(item: TrailerReservation): MutableList<String> {
        if (item.archived && item.batchSize > limit) {
            return mutableListOf<String>("batch size ${item.batchSize} exceeds $limit while archived")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a trailer reservation. */
data class TrailerReservationValidator(var rules: MutableList<TrailerReservationRule>) {
    fun validate(item: TrailerReservation): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: TrailerReservation) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TrailerReservation>): MutableMap<String, MutableList<String>> {
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

fun defaultTrailerReservationValidator(): TrailerReservationValidator {
    return TrailerReservationValidator(
        mutableListOf<TrailerReservationRule>(
            TrailerReservationRequiredCodeRule("trailer reservation needs an entry"),
            TrailerReservationRequiredSummaryRule("trailer reservation needs a value"),
            TrailerReservationCodeLengthRule(3600),
            TrailerReservationRevisionRangeRule(32, 120),
            TrailerReservationArchivedGuardRule(1000),
        ),
    )
}
