package com.acme.transport.crewing.qualification.reservation

/** A rule a capped qualification reservation must pass. Check returns its problems, or none. */
interface CappedQualificationReservationRule {
    fun check(item: CappedQualificationReservation): MutableList<String>
}

data class CappedQualificationReservationCountryLengthRule(
    var maxLength: Int,
) : CappedQualificationReservationRule {
    override fun check(item: CappedQualificationReservation): MutableList<String> {
        val size = item.country.length
        if (size > maxLength) {
            return mutableListOf<String>("country is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CappedQualificationReservationRateRangeRule(
    var min: Double,
    var max: Double,
) : CappedQualificationReservationRule {
    override fun check(item: CappedQualificationReservation): MutableList<String> {
        val value = item.rate
        if (value < min || value > max) {
            return mutableListOf<String>("rate $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class CappedQualificationReservationTitleLengthRule(
    var maxLength: Int,
) : CappedQualificationReservationRule {
    override fun check(item: CappedQualificationReservation): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CappedQualificationReservationRequiredTimezoneRule(
    var message: String,
) : CappedQualificationReservationRule {
    override fun check(item: CappedQualificationReservation): MutableList<String> {
        if (item.timezone.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CappedQualificationReservationCodeLengthRule(
    var maxLength: Int,
) : CappedQualificationReservationRule {
    override fun check(item: CappedQualificationReservation): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CappedQualificationReservationTimezoneLengthRule(
    var maxLength: Int,
) : CappedQualificationReservationRule {
    override fun check(item: CappedQualificationReservation): MutableList<String> {
        val size = item.timezone.length
        if (size > maxLength) {
            return mutableListOf<String>("timezone is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a capped qualification reservation. */
data class CappedQualificationReservationValidator(var rules: MutableList<CappedQualificationReservationRule>) {
    fun validate(item: CappedQualificationReservation): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: CappedQualificationReservation) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CappedQualificationReservation>): MutableMap<String, MutableList<String>> {
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

fun defaultCappedQualificationReservationValidator(): CappedQualificationReservationValidator {
    return CappedQualificationReservationValidator(
        mutableListOf<CappedQualificationReservationRule>(
            CappedQualificationReservationCountryLengthRule(64),
            CappedQualificationReservationRateRangeRule(6.9, 1970.3),
            CappedQualificationReservationTitleLengthRule(255),
            CappedQualificationReservationRequiredTimezoneRule("capped qualification reservation needs a value"),
            CappedQualificationReservationCodeLengthRule(120),
            CappedQualificationReservationTimezoneLengthRule(3600),
        ),
    )
}
