package com.acme.transport.customs.inspection.draft

/** A rule a inspection reservation must pass. Check returns its problems, or none. */
interface InspectionReservationRule {
    fun check(item: InspectionReservation): MutableList<String>
}

data class InspectionReservationLabelLengthRule(var maxLength: Int) : InspectionReservationRule {
    override fun check(item: InspectionReservation): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class InspectionReservationCodePrefixRule(var prefix: String) : InspectionReservationRule {
    override fun check(item: InspectionReservation): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class InspectionReservationRetriesRangeRule(var min: Int, var max: Int) : InspectionReservationRule {
    override fun check(item: InspectionReservation): MutableList<String> {
        val value = item.retries
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("retries $value is below $min")
        }
        if (value > max) {
            problems.add("retries $value is above $max")
        }
        return problems
    }
}

data class InspectionReservationCodeLengthRule(var maxLength: Int) : InspectionReservationRule {
    override fun check(item: InspectionReservation): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class InspectionReservationRequiredCodeRule(var message: String) : InspectionReservationRule {
    override fun check(item: InspectionReservation): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a inspection reservation. */
data class InspectionReservationValidator(var rules: MutableList<InspectionReservationRule>) {
    fun validate(item: InspectionReservation): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: InspectionReservation) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<InspectionReservation>): MutableMap<String, MutableList<String>> {
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

fun defaultInspectionReservationValidator(): InspectionReservationValidator {
    return InspectionReservationValidator(
        mutableListOf<InspectionReservationRule>(
            InspectionReservationLabelLengthRule(1000),
            InspectionReservationCodePrefixRule("REF"),
            InspectionReservationRetriesRangeRule(1000, 3600),
            InspectionReservationCodeLengthRule(80),
            InspectionReservationRequiredCodeRule("inspection reservation needs a value"),
        ),
    )
}
