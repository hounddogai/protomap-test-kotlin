package com.acme.transport.freightpay.ratecard.regionalcardsnapshot

/** A rule a strict rate card reservation must pass. Check returns its problems, or none. */
interface StrictRateCardReservationRule {
    fun check(item: StrictRateCardReservation): MutableList<String>
}

data class StrictRateCardReservationArchivedGuardRule(var limit: Int) : StrictRateCardReservationRule {
    override fun check(item: StrictRateCardReservation): MutableList<String> {
        if (item.archived && item.etaMinutes > limit) {
            return mutableListOf<String>("eta minutes ${item.etaMinutes} exceeds $limit while archived")
        }
        return mutableListOf<String>()
    }
}

data class StrictRateCardReservationRequiredPhoneRule(var message: String) : StrictRateCardReservationRule {
    override fun check(item: StrictRateCardReservation): MutableList<String> {
        if (item.phone.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class StrictRateCardReservationRequiredLabelRule(var message: String) : StrictRateCardReservationRule {
    override fun check(item: StrictRateCardReservation): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class StrictRateCardReservationCodeLengthRule(var maxLength: Int) : StrictRateCardReservationRule {
    override fun check(item: StrictRateCardReservation): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a strict rate card reservation. */
data class StrictRateCardReservationValidator(var rules: MutableList<StrictRateCardReservationRule>) {
    fun validate(item: StrictRateCardReservation): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: StrictRateCardReservation) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<StrictRateCardReservation>): MutableMap<String, MutableList<String>> {
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

fun defaultStrictRateCardReservationValidator(): StrictRateCardReservationValidator {
    return StrictRateCardReservationValidator(
        mutableListOf<StrictRateCardReservationRule>(
            StrictRateCardReservationArchivedGuardRule(255),
            StrictRateCardReservationRequiredPhoneRule("strict rate card reservation needs a value"),
            StrictRateCardReservationRequiredLabelRule("strict rate card reservation needs a value"),
            StrictRateCardReservationCodeLengthRule(255),
        ),
    )
}
