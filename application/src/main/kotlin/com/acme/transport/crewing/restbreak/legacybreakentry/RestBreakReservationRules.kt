package com.acme.transport.crewing.restbreak.legacybreakentry

/** A rule a rest break reservation must pass. Check returns its problems, or none. */
interface RestBreakReservationRule {
    fun check(item: RestBreakReservation): MutableList<String>
}

data class RestBreakReservationCodeLengthRule(var maxLength: Int) : RestBreakReservationRule {
    override fun check(item: RestBreakReservation): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RestBreakReservationCodePrefixRule(var prefix: String) : RestBreakReservationRule {
    override fun check(item: RestBreakReservation): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class RestBreakReservationTitleLengthRule(var maxLength: Int) : RestBreakReservationRule {
    override fun check(item: RestBreakReservation): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a rest break reservation. */
data class RestBreakReservationValidator(var rules: MutableList<RestBreakReservationRule>) {
    fun validate(item: RestBreakReservation): MutableList<String> {
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

    fun requireValid(item: RestBreakReservation) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RestBreakReservation>): MutableMap<String, MutableList<String>> {
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

fun defaultRestBreakReservationValidator(): RestBreakReservationValidator {
    return RestBreakReservationValidator(
        mutableListOf<RestBreakReservationRule>(
            RestBreakReservationCodeLengthRule(86400),
            RestBreakReservationCodePrefixRule("REF"),
            RestBreakReservationTitleLengthRule(64),
        ),
    )
}
