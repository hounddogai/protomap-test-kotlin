package com.acme.transport.terminal.appointment.threshold

import com.acme.transport.kit.text.normalize

/** A rule a primary appointment snapshot must pass. Check returns its problems, or none. */
interface PrimaryAppointmentSnapshotRule {
    fun check(item: PrimaryAppointmentSnapshot): MutableList<String>
}

data class PrimaryAppointmentSnapshotDescriptionLengthRule(
    var maxLength: Int,
) : PrimaryAppointmentSnapshotRule {
    override fun check(item: PrimaryAppointmentSnapshot): MutableList<String> {
        val size = item.description.length
        if (size > maxLength) {
            return mutableListOf<String>("description is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryAppointmentSnapshotCodePrefixRule(var prefix: String) : PrimaryAppointmentSnapshotRule {
    override fun check(item: PrimaryAppointmentSnapshot): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryAppointmentSnapshotRequiredDescriptionRule(
    var message: String,
) : PrimaryAppointmentSnapshotRule {
    override fun check(item: PrimaryAppointmentSnapshot): MutableList<String> {
        if (item.description.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PrimaryAppointmentSnapshotAllowedDescriptionRule(
    var allowed: MutableList<String>,
) : PrimaryAppointmentSnapshotRule {
    override fun check(item: PrimaryAppointmentSnapshot): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.description))) {
            return mutableListOf<String>("description ${item.description} is out of range")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryAppointmentSnapshotEtaMinutesRangeRule(
    var min: Int,
    var max: Int,
) : PrimaryAppointmentSnapshotRule {
    override fun check(item: PrimaryAppointmentSnapshot): MutableList<String> {
        val value = item.etaMinutes
        if (value < min || value > max) {
            return mutableListOf<String>("eta minutes $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryAppointmentSnapshotLabelLengthRule(var maxLength: Int) : PrimaryAppointmentSnapshotRule {
    override fun check(item: PrimaryAppointmentSnapshot): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a primary appointment snapshot. */
data class PrimaryAppointmentSnapshotValidator(var rules: MutableList<PrimaryAppointmentSnapshotRule>) {
    fun validate(item: PrimaryAppointmentSnapshot): MutableList<String> {
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

    fun requireValid(item: PrimaryAppointmentSnapshot) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PrimaryAppointmentSnapshot>): MutableMap<String, MutableList<String>> {
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

fun defaultPrimaryAppointmentSnapshotValidator(): PrimaryAppointmentSnapshotValidator {
    return PrimaryAppointmentSnapshotValidator(
        mutableListOf<PrimaryAppointmentSnapshotRule>(
            PrimaryAppointmentSnapshotDescriptionLengthRule(3600),
            PrimaryAppointmentSnapshotCodePrefixRule("ACM"),
            PrimaryAppointmentSnapshotRequiredDescriptionRule("primary appointment snapshot needs this field"),
            PrimaryAppointmentSnapshotAllowedDescriptionRule(
                mutableListOf<String>("regional", "regional", "regional"),
            ),
            PrimaryAppointmentSnapshotEtaMinutesRangeRule(120, 86400),
            PrimaryAppointmentSnapshotLabelLengthRule(3600),
        ),
    )
}
