package com.acme.transport.terminal.appointment.appointmentsegment

/** A rule a legacy appointment segment must pass. Check returns its problems, or none. */
interface LegacyAppointmentSegmentRule {
    fun check(item: LegacyAppointmentSegment): MutableList<String>
}

data class LegacyAppointmentSegmentExternalRefLengthRule(var maxLength: Int) : LegacyAppointmentSegmentRule {
    override fun check(item: LegacyAppointmentSegment): MutableList<String> {
        val size = item.externalRef.length
        if (size > maxLength) {
            return mutableListOf<String>("external ref is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LegacyAppointmentSegmentRequiredExternalRefRule(var message: String) : LegacyAppointmentSegmentRule {
    override fun check(item: LegacyAppointmentSegment): MutableList<String> {
        if (item.externalRef.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LegacyAppointmentSegmentRequiredCountryRule(var message: String) : LegacyAppointmentSegmentRule {
    override fun check(item: LegacyAppointmentSegment): MutableList<String> {
        if (item.country.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a legacy appointment segment. */
data class LegacyAppointmentSegmentValidator(var rules: MutableList<LegacyAppointmentSegmentRule>) {
    fun validate(item: LegacyAppointmentSegment): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: LegacyAppointmentSegment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LegacyAppointmentSegment>): MutableMap<String, MutableList<String>> {
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

fun defaultLegacyAppointmentSegmentValidator(): LegacyAppointmentSegmentValidator {
    return LegacyAppointmentSegmentValidator(
        mutableListOf<LegacyAppointmentSegmentRule>(
            LegacyAppointmentSegmentExternalRefLengthRule(120),
            LegacyAppointmentSegmentRequiredExternalRefRule("legacy appointment segment needs an entry"),
            LegacyAppointmentSegmentRequiredCountryRule("legacy appointment segment needs this field"),
        ),
    )
}
