package com.acme.transport.terminal.traileryard.reservation

/** A rule a legacy trailer yard reservation must pass. Check returns its problems, or none. */
interface LegacyTrailerYardReservationRule {
    fun check(item: LegacyTrailerYardReservation): MutableList<String>
}

data class LegacyTrailerYardReservationSummaryLengthRule(
    var maxLength: Int,
) : LegacyTrailerYardReservationRule {
    override fun check(item: LegacyTrailerYardReservation): MutableList<String> {
        val size = item.summary.length
        if (size > maxLength) {
            return mutableListOf<String>("summary is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LegacyTrailerYardReservationCodeLengthRule(var maxLength: Int) : LegacyTrailerYardReservationRule {
    override fun check(item: LegacyTrailerYardReservation): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LegacyTrailerYardReservationRequiredCodeRule(
    var message: String,
) : LegacyTrailerYardReservationRule {
    override fun check(item: LegacyTrailerYardReservation): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a legacy trailer yard reservation. */
data class LegacyTrailerYardReservationValidator(var rules: MutableList<LegacyTrailerYardReservationRule>) {
    fun validate(item: LegacyTrailerYardReservation): MutableList<String> {
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

    fun requireValid(item: LegacyTrailerYardReservation) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LegacyTrailerYardReservation>): MutableMap<String, MutableList<String>> {
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

fun defaultLegacyTrailerYardReservationValidator(): LegacyTrailerYardReservationValidator {
    return LegacyTrailerYardReservationValidator(
        mutableListOf<LegacyTrailerYardReservationRule>(
            LegacyTrailerYardReservationSummaryLengthRule(32),
            LegacyTrailerYardReservationCodeLengthRule(1000),
            LegacyTrailerYardReservationRequiredCodeRule("legacy trailer yard reservation needs a value"),
        ),
    )
}
