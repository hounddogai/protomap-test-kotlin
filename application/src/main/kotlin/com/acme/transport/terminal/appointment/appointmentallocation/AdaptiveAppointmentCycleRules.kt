package com.acme.transport.terminal.appointment.appointmentallocation

import com.acme.transport.kit.text.normalize

/** A rule a adaptive appointment cycle must pass. Check returns its problems, or none. */
interface AdaptiveAppointmentCycleRule {
    fun check(item: AdaptiveAppointmentCycle): MutableList<String>
}

data class AdaptiveAppointmentCycleVisibleGuardRule(var limit: Int) : AdaptiveAppointmentCycleRule {
    override fun check(item: AdaptiveAppointmentCycle): MutableList<String> {
        if (item.visible && item.startsAt > limit) {
            return mutableListOf<String>("starts at ${item.startsAt} exceeds $limit while visible")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveAppointmentCycleLabelLengthRule(var maxLength: Int) : AdaptiveAppointmentCycleRule {
    override fun check(item: AdaptiveAppointmentCycle): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveAppointmentCycleAllowedCodeRule(
    var allowed: MutableList<String>,
) : AdaptiveAppointmentCycleRule {
    override fun check(item: AdaptiveAppointmentCycle): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is expired")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveAppointmentCycleStartsAtRangeRule(
    var min: Int,
    var max: Int,
) : AdaptiveAppointmentCycleRule {
    override fun check(item: AdaptiveAppointmentCycle): MutableList<String> {
        val value = item.startsAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("starts at $value is below $min")
        }
        if (value > max) {
            problems.add("starts at $value is above $max")
        }
        return problems
    }
}

data class AdaptiveAppointmentCycleRequiredCodeRule(var message: String) : AdaptiveAppointmentCycleRule {
    override fun check(item: AdaptiveAppointmentCycle): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveAppointmentCycleRequiredNotesRule(var message: String) : AdaptiveAppointmentCycleRule {
    override fun check(item: AdaptiveAppointmentCycle): MutableList<String> {
        if (item.notes.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a adaptive appointment cycle. */
data class AdaptiveAppointmentCycleValidator(var rules: MutableList<AdaptiveAppointmentCycleRule>) {
    fun validate(item: AdaptiveAppointmentCycle): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: AdaptiveAppointmentCycle) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AdaptiveAppointmentCycle>): MutableMap<String, MutableList<String>> {
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

fun defaultAdaptiveAppointmentCycleValidator(): AdaptiveAppointmentCycleValidator {
    return AdaptiveAppointmentCycleValidator(
        mutableListOf<AdaptiveAppointmentCycleRule>(
            AdaptiveAppointmentCycleVisibleGuardRule(255),
            AdaptiveAppointmentCycleLabelLengthRule(255),
            AdaptiveAppointmentCycleAllowedCodeRule(mutableListOf<String>("lenient")),
            AdaptiveAppointmentCycleStartsAtRangeRule(255, 255),
            AdaptiveAppointmentCycleRequiredCodeRule("adaptive appointment cycle needs an entry"),
            AdaptiveAppointmentCycleRequiredNotesRule("adaptive appointment cycle needs this field"),
        ),
    )
}
