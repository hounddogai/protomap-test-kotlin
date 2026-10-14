package com.acme.transport.terminal.appointment.snapshot

import com.acme.transport.kit.text.normalize

/** A rule a weighted appointment segment must pass. Check returns its problems, or none. */
interface WeightedAppointmentSegmentRule {
    fun check(item: WeightedAppointmentSegment): MutableList<String>
}

data class WeightedAppointmentSegmentAllowedChannelRule(
    var allowed: MutableList<String>,
) : WeightedAppointmentSegmentRule {
    override fun check(item: WeightedAppointmentSegment): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.channel))) {
            return mutableListOf<String>("channel ${item.channel} is missing")
        }
        return mutableListOf<String>()
    }
}

data class WeightedAppointmentSegmentCodeLengthRule(var maxLength: Int) : WeightedAppointmentSegmentRule {
    override fun check(item: WeightedAppointmentSegment): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class WeightedAppointmentSegmentTitleLengthRule(var maxLength: Int) : WeightedAppointmentSegmentRule {
    override fun check(item: WeightedAppointmentSegment): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class WeightedAppointmentSegmentRequiredContactEmailRule(
    var message: String,
) : WeightedAppointmentSegmentRule {
    override fun check(item: WeightedAppointmentSegment): MutableList<String> {
        if (item.contactEmail.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a weighted appointment segment. */
data class WeightedAppointmentSegmentValidator(var rules: MutableList<WeightedAppointmentSegmentRule>) {
    fun validate(item: WeightedAppointmentSegment): MutableList<String> {
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

    fun requireValid(item: WeightedAppointmentSegment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<WeightedAppointmentSegment>): MutableMap<String, MutableList<String>> {
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

fun defaultWeightedAppointmentSegmentValidator(): WeightedAppointmentSegmentValidator {
    return WeightedAppointmentSegmentValidator(
        mutableListOf<WeightedAppointmentSegmentRule>(
            WeightedAppointmentSegmentAllowedChannelRule(mutableListOf<String>()),
            WeightedAppointmentSegmentCodeLengthRule(32),
            WeightedAppointmentSegmentTitleLengthRule(255),
            WeightedAppointmentSegmentRequiredContactEmailRule("weighted appointment segment needs a value"),
        ),
    )
}
