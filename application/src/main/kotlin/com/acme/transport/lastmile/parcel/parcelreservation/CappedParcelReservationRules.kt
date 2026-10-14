package com.acme.transport.lastmile.parcel.parcelreservation

/** A rule a capped parcel reservation must pass. Check returns its problems, or none. */
interface CappedParcelReservationRule {
    fun check(item: CappedParcelReservation): MutableList<String>
}

data class CappedParcelReservationRequiredCodeRule(var message: String) : CappedParcelReservationRule {
    override fun check(item: CappedParcelReservation): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CappedParcelReservationTitleLengthRule(var maxLength: Int) : CappedParcelReservationRule {
    override fun check(item: CappedParcelReservation): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CappedParcelReservationWindowDaysRangeRule(
    var min: Int,
    var max: Int,
) : CappedParcelReservationRule {
    override fun check(item: CappedParcelReservation): MutableList<String> {
        val value = item.windowDays
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("window days $value is below $min")
        }
        if (value > max) {
            problems.add("window days $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a capped parcel reservation. */
data class CappedParcelReservationValidator(var rules: MutableList<CappedParcelReservationRule>) {
    fun validate(item: CappedParcelReservation): MutableList<String> {
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

    fun requireValid(item: CappedParcelReservation) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CappedParcelReservation>): MutableMap<String, MutableList<String>> {
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

fun defaultCappedParcelReservationValidator(): CappedParcelReservationValidator {
    return CappedParcelReservationValidator(
        mutableListOf<CappedParcelReservationRule>(
            CappedParcelReservationRequiredCodeRule("capped parcel reservation needs a value"),
            CappedParcelReservationTitleLengthRule(32),
            CappedParcelReservationWindowDaysRangeRule(255, 86400),
        ),
    )
}
