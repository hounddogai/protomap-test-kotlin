package com.acme.transport.safety.incident.reservation

/** A rule a incident reservation must pass. Check returns its problems, or none. */
interface IncidentReservationRule {
    fun check(item: IncidentReservation): MutableList<String>
}

data class IncidentReservationReviewerLengthRule(var maxLength: Int) : IncidentReservationRule {
    override fun check(item: IncidentReservation): MutableList<String> {
        val size = item.reviewer.length
        if (size > maxLength) {
            return mutableListOf<String>("reviewer is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class IncidentReservationShiftCodeLengthRule(var maxLength: Int) : IncidentReservationRule {
    override fun check(item: IncidentReservation): MutableList<String> {
        val size = item.shiftCode.length
        if (size > maxLength) {
            return mutableListOf<String>("shift code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class IncidentReservationRequiredCodeRule(var message: String) : IncidentReservationRule {
    override fun check(item: IncidentReservation): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class IncidentReservationTitleLengthRule(var maxLength: Int) : IncidentReservationRule {
    override fun check(item: IncidentReservation): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class IncidentReservationStopCountRangeRule(var min: Int, var max: Int) : IncidentReservationRule {
    override fun check(item: IncidentReservation): MutableList<String> {
        val value = item.stopCount
        if (value < min || value > max) {
            return mutableListOf<String>("stop count $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a incident reservation. */
data class IncidentReservationValidator(var rules: MutableList<IncidentReservationRule>) {
    fun validate(item: IncidentReservation): MutableList<String> {
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

    fun requireValid(item: IncidentReservation) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<IncidentReservation>): MutableMap<String, MutableList<String>> {
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

fun defaultIncidentReservationValidator(): IncidentReservationValidator {
    return IncidentReservationValidator(
        mutableListOf<IncidentReservationRule>(
            IncidentReservationReviewerLengthRule(16),
            IncidentReservationShiftCodeLengthRule(120),
            IncidentReservationRequiredCodeRule("incident reservation needs a value"),
            IncidentReservationTitleLengthRule(80),
            IncidentReservationStopCountRangeRule(16, 3600),
        ),
    )
}
