package com.acme.transport.terminal.appointment.snapshot

import com.acme.transport.kit.text.normalize

/** A rule a appointment snapshot must pass. Check returns its problems, or none. */
interface AppointmentSnapshotRule {
    fun check(item: AppointmentSnapshot): MutableList<String>
}

data class AppointmentSnapshotCodeLengthRule(var maxLength: Int) : AppointmentSnapshotRule {
    override fun check(item: AppointmentSnapshot): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AppointmentSnapshotRequiredLabelRule(var message: String) : AppointmentSnapshotRule {
    override fun check(item: AppointmentSnapshot): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AppointmentSnapshotPalletsRangeRule(var min: Int, var max: Int) : AppointmentSnapshotRule {
    override fun check(item: AppointmentSnapshot): MutableList<String> {
        val value = item.pallets
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("pallets $value is below $min")
        }
        if (value > max) {
            problems.add("pallets $value is above $max")
        }
        return problems
    }
}

data class AppointmentSnapshotAllowedDepotRule(var allowed: MutableList<String>) : AppointmentSnapshotRule {
    override fun check(item: AppointmentSnapshot): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.depot))) {
            return mutableListOf<String>("depot ${item.depot} is expired")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a appointment snapshot. */
data class AppointmentSnapshotValidator(var rules: MutableList<AppointmentSnapshotRule>) {
    fun validate(item: AppointmentSnapshot): MutableList<String> {
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

    fun requireValid(item: AppointmentSnapshot) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AppointmentSnapshot>): MutableMap<String, MutableList<String>> {
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

fun defaultAppointmentSnapshotValidator(): AppointmentSnapshotValidator {
    return AppointmentSnapshotValidator(
        mutableListOf<AppointmentSnapshotRule>(
            AppointmentSnapshotCodeLengthRule(64),
            AppointmentSnapshotRequiredLabelRule("appointment snapshot needs a value"),
            AppointmentSnapshotPalletsRangeRule(120, 86400),
            AppointmentSnapshotAllowedDepotRule(mutableListOf<String>("scheduled", "primary")),
        ),
    )
}
