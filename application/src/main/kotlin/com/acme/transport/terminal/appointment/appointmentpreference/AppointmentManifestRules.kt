package com.acme.transport.terminal.appointment.appointmentpreference

/** A rule a appointment manifest must pass. Check returns its problems, or none. */
interface AppointmentManifestRule {
    fun check(item: AppointmentManifest): MutableList<String>
}

data class AppointmentManifestCodePrefixRule(var prefix: String) : AppointmentManifestRule {
    override fun check(item: AppointmentManifest): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class AppointmentManifestRequiredCodeRule(var message: String) : AppointmentManifestRule {
    override fun check(item: AppointmentManifest): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AppointmentManifestShiftCodeLengthRule(var maxLength: Int) : AppointmentManifestRule {
    override fun check(item: AppointmentManifest): MutableList<String> {
        val size = item.shiftCode.length
        if (size > maxLength) {
            return mutableListOf<String>("shift code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AppointmentManifestCreatedAtRangeRule(var min: Int, var max: Int) : AppointmentManifestRule {
    override fun check(item: AppointmentManifest): MutableList<String> {
        val value = item.createdAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("created at $value is below $min")
        }
        if (value > max) {
            problems.add("created at $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a appointment manifest. */
data class AppointmentManifestValidator(var rules: MutableList<AppointmentManifestRule>) {
    fun validate(item: AppointmentManifest): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: AppointmentManifest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AppointmentManifest>): MutableMap<String, MutableList<String>> {
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

fun defaultAppointmentManifestValidator(): AppointmentManifestValidator {
    return AppointmentManifestValidator(
        mutableListOf<AppointmentManifestRule>(
            AppointmentManifestCodePrefixRule("REF"),
            AppointmentManifestRequiredCodeRule("appointment manifest needs a value"),
            AppointmentManifestShiftCodeLengthRule(32),
            AppointmentManifestCreatedAtRangeRule(120, 86400),
        ),
    )
}
