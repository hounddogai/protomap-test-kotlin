package com.acme.transport.terminal.appointment.adaptiveappointmentmapping

/** A rule a strict appointment entry must pass. Check returns its problems, or none. */
interface StrictAppointmentEntryRule {
    fun check(item: StrictAppointmentEntry): MutableList<String>
}

data class StrictAppointmentEntryRequiredLabelRule(var message: String) : StrictAppointmentEntryRule {
    override fun check(item: StrictAppointmentEntry): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class StrictAppointmentEntryLabelLengthRule(var maxLength: Int) : StrictAppointmentEntryRule {
    override fun check(item: StrictAppointmentEntry): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class StrictAppointmentEntryFloorValueRangeRule(
    var min: Double,
    var max: Double,
) : StrictAppointmentEntryRule {
    override fun check(item: StrictAppointmentEntry): MutableList<String> {
        val value = item.floorValue
        if (value < min || value > max) {
            return mutableListOf<String>("floor value $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class StrictAppointmentEntryCodePrefixRule(var prefix: String) : StrictAppointmentEntryRule {
    override fun check(item: StrictAppointmentEntry): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class StrictAppointmentEntryStopCountRangeRule(var min: Int, var max: Int) : StrictAppointmentEntryRule {
    override fun check(item: StrictAppointmentEntry): MutableList<String> {
        val value = item.stopCount
        if (value < min || value > max) {
            return mutableListOf<String>("stop count $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class StrictAppointmentEntryRequiredCodeRule(var message: String) : StrictAppointmentEntryRule {
    override fun check(item: StrictAppointmentEntry): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a strict appointment entry. */
data class StrictAppointmentEntryValidator(var rules: MutableList<StrictAppointmentEntryRule>) {
    fun validate(item: StrictAppointmentEntry): MutableList<String> {
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

    fun requireValid(item: StrictAppointmentEntry) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<StrictAppointmentEntry>): MutableMap<String, MutableList<String>> {
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

fun defaultStrictAppointmentEntryValidator(): StrictAppointmentEntryValidator {
    return StrictAppointmentEntryValidator(
        mutableListOf<StrictAppointmentEntryRule>(
            StrictAppointmentEntryRequiredLabelRule("strict appointment entry needs this field"),
            StrictAppointmentEntryLabelLengthRule(64),
            StrictAppointmentEntryFloorValueRangeRule(1.1, 4503.7),
            StrictAppointmentEntryCodePrefixRule("X"),
            StrictAppointmentEntryStopCountRangeRule(120, 3600),
            StrictAppointmentEntryRequiredCodeRule("strict appointment entry needs this field"),
        ),
    )
}
