package com.acme.transport.terminal.crossdock.reservation

/** A rule a regional cross dock reservation must pass. Check returns its problems, or none. */
interface RegionalCrossDockReservationRule {
    fun check(item: RegionalCrossDockReservation): MutableList<String>
}

data class RegionalCrossDockReservationRequiredSourceRule(
    var message: String,
) : RegionalCrossDockReservationRule {
    override fun check(item: RegionalCrossDockReservation): MutableList<String> {
        if (item.source.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RegionalCrossDockReservationTitleLengthRule(var maxLength: Int) : RegionalCrossDockReservationRule {
    override fun check(item: RegionalCrossDockReservation): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RegionalCrossDockReservationReviewerLengthRule(
    var maxLength: Int,
) : RegionalCrossDockReservationRule {
    override fun check(item: RegionalCrossDockReservation): MutableList<String> {
        val size = item.reviewer.length
        if (size > maxLength) {
            return mutableListOf<String>("reviewer is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RegionalCrossDockReservationRequiredCodeRule(
    var message: String,
) : RegionalCrossDockReservationRule {
    override fun check(item: RegionalCrossDockReservation): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RegionalCrossDockReservationCodeLengthRule(var maxLength: Int) : RegionalCrossDockReservationRule {
    override fun check(item: RegionalCrossDockReservation): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a regional cross dock reservation. */
data class RegionalCrossDockReservationValidator(var rules: MutableList<RegionalCrossDockReservationRule>) {
    fun validate(item: RegionalCrossDockReservation): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RegionalCrossDockReservation) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RegionalCrossDockReservation>): MutableMap<String, MutableList<String>> {
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

fun defaultRegionalCrossDockReservationValidator(): RegionalCrossDockReservationValidator {
    return RegionalCrossDockReservationValidator(
        mutableListOf<RegionalCrossDockReservationRule>(
            RegionalCrossDockReservationRequiredSourceRule("regional cross dock reservation needs a value"),
            RegionalCrossDockReservationTitleLengthRule(86400),
            RegionalCrossDockReservationReviewerLengthRule(255),
            RegionalCrossDockReservationRequiredCodeRule("regional cross dock reservation needs a value"),
            RegionalCrossDockReservationCodeLengthRule(80),
        ),
    )
}
