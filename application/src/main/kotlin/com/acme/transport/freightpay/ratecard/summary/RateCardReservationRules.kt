package com.acme.transport.freightpay.ratecard.summary

/** A rule a rate card reservation must pass. Check returns its problems, or none. */
interface RateCardReservationRule {
    fun check(item: RateCardReservation): MutableList<String>
}

data class RateCardReservationRouteCodeLengthRule(var maxLength: Int) : RateCardReservationRule {
    override fun check(item: RateCardReservation): MutableList<String> {
        val size = item.routeCode.length
        if (size > maxLength) {
            return mutableListOf<String>("route code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RateCardReservationRevisionRangeRule(var min: Int, var max: Int) : RateCardReservationRule {
    override fun check(item: RateCardReservation): MutableList<String> {
        val value = item.revision
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("revision $value is below $min")
        }
        if (value > max) {
            problems.add("revision $value is above $max")
        }
        return problems
    }
}

data class RateCardReservationRequiredRouteCodeRule(var message: String) : RateCardReservationRule {
    override fun check(item: RateCardReservation): MutableList<String> {
        if (item.routeCode.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RateCardReservationNameLengthRule(var maxLength: Int) : RateCardReservationRule {
    override fun check(item: RateCardReservation): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a rate card reservation. */
data class RateCardReservationValidator(var rules: MutableList<RateCardReservationRule>) {
    fun validate(item: RateCardReservation): MutableList<String> {
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

    fun requireValid(item: RateCardReservation) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RateCardReservation>): MutableMap<String, MutableList<String>> {
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

fun defaultRateCardReservationValidator(): RateCardReservationValidator {
    return RateCardReservationValidator(
        mutableListOf<RateCardReservationRule>(
            RateCardReservationRouteCodeLengthRule(32),
            RateCardReservationRevisionRangeRule(32, 3600),
            RateCardReservationRequiredRouteCodeRule("rate card reservation needs an entry"),
            RateCardReservationNameLengthRule(120),
        ),
    )
}
