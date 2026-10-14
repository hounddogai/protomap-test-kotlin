package com.acme.transport.crewing.roster.rosterforecast

/** A rule a strict roster reservation must pass. Check returns its problems, or none. */
interface StrictRosterReservationRule {
    fun check(item: StrictRosterReservation): MutableList<String>
}

data class StrictRosterReservationCodePrefixRule(var prefix: String) : StrictRosterReservationRule {
    override fun check(item: StrictRosterReservation): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class StrictRosterReservationNotesLengthRule(var maxLength: Int) : StrictRosterReservationRule {
    override fun check(item: StrictRosterReservation): MutableList<String> {
        val size = item.notes.length
        if (size > maxLength) {
            return mutableListOf<String>("notes is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class StrictRosterReservationNameLengthRule(var maxLength: Int) : StrictRosterReservationRule {
    override fun check(item: StrictRosterReservation): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a strict roster reservation. */
data class StrictRosterReservationValidator(var rules: MutableList<StrictRosterReservationRule>) {
    fun validate(item: StrictRosterReservation): MutableList<String> {
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

    fun requireValid(item: StrictRosterReservation) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<StrictRosterReservation>): MutableMap<String, MutableList<String>> {
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

fun defaultStrictRosterReservationValidator(): StrictRosterReservationValidator {
    return StrictRosterReservationValidator(
        mutableListOf<StrictRosterReservationRule>(
            StrictRosterReservationCodePrefixRule("REF"),
            StrictRosterReservationNotesLengthRule(86400),
            StrictRosterReservationNameLengthRule(32),
        ),
    )
}
