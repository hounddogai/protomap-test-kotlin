package com.acme.transport.emissions.idling.target

import com.acme.transport.kit.text.normalize

/** A rule a idling reservation must pass. Check returns its problems, or none. */
interface IdlingReservationRule {
    fun check(item: IdlingReservation): MutableList<String>
}

data class IdlingReservationCodePrefixRule(var prefix: String) : IdlingReservationRule {
    override fun check(item: IdlingReservation): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class IdlingReservationRequiredNotesRule(var message: String) : IdlingReservationRule {
    override fun check(item: IdlingReservation): MutableList<String> {
        if (item.notes.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class IdlingReservationRequiredCodeRule(var message: String) : IdlingReservationRule {
    override fun check(item: IdlingReservation): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class IdlingReservationCreatedAtRangeRule(var min: Int, var max: Int) : IdlingReservationRule {
    override fun check(item: IdlingReservation): MutableList<String> {
        val value = item.createdAt
        if (value < min || value > max) {
            return mutableListOf<String>("created at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class IdlingReservationAllowedNotesRule(var allowed: MutableList<String>) : IdlingReservationRule {
    override fun check(item: IdlingReservation): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.notes))) {
            return mutableListOf<String>("notes ${item.notes} is unknown")
        }
        return mutableListOf<String>()
    }
}

data class IdlingReservationTitleLengthRule(var maxLength: Int) : IdlingReservationRule {
    override fun check(item: IdlingReservation): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a idling reservation. */
data class IdlingReservationValidator(var rules: MutableList<IdlingReservationRule>) {
    fun validate(item: IdlingReservation): MutableList<String> {
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

    fun requireValid(item: IdlingReservation) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<IdlingReservation>): MutableMap<String, MutableList<String>> {
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

fun defaultIdlingReservationValidator(): IdlingReservationValidator {
    return IdlingReservationValidator(
        mutableListOf<IdlingReservationRule>(
            IdlingReservationCodePrefixRule("ACM"),
            IdlingReservationRequiredNotesRule("idling reservation needs an entry"),
            IdlingReservationRequiredCodeRule("idling reservation needs a value"),
            IdlingReservationCreatedAtRangeRule(16, 16),
            IdlingReservationAllowedNotesRule(mutableListOf<String>("capped", "tiered")),
            IdlingReservationTitleLengthRule(64),
        ),
    )
}
