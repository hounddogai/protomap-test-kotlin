package com.acme.transport.terminal.appointment.appointmentscore

import com.acme.transport.kit.text.normalize

/** A rule a rolling appointment snapshot must pass. Check returns its problems, or none. */
interface RollingAppointmentSnapshotRule {
    fun check(item: RollingAppointmentSnapshot): MutableList<String>
}

data class RollingAppointmentSnapshotRequiredCodeRule(var message: String) : RollingAppointmentSnapshotRule {
    override fun check(item: RollingAppointmentSnapshot): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RollingAppointmentSnapshotCodePrefixRule(var prefix: String) : RollingAppointmentSnapshotRule {
    override fun check(item: RollingAppointmentSnapshot): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class RollingAppointmentSnapshotAllowedChannelRule(
    var allowed: MutableList<String>,
) : RollingAppointmentSnapshotRule {
    override fun check(item: RollingAppointmentSnapshot): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.channel))) {
            return mutableListOf<String>("channel ${item.channel} is duplicated")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a rolling appointment snapshot. */
data class RollingAppointmentSnapshotValidator(var rules: MutableList<RollingAppointmentSnapshotRule>) {
    fun validate(item: RollingAppointmentSnapshot): MutableList<String> {
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

    fun requireValid(item: RollingAppointmentSnapshot) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RollingAppointmentSnapshot>): MutableMap<String, MutableList<String>> {
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

fun defaultRollingAppointmentSnapshotValidator(): RollingAppointmentSnapshotValidator {
    return RollingAppointmentSnapshotValidator(
        mutableListOf<RollingAppointmentSnapshotRule>(
            RollingAppointmentSnapshotRequiredCodeRule("rolling appointment snapshot needs an entry"),
            RollingAppointmentSnapshotCodePrefixRule("X"),
            RollingAppointmentSnapshotAllowedChannelRule(mutableListOf<String>("pending")),
        ),
    )
}
