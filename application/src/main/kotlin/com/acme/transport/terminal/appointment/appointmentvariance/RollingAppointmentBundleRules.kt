package com.acme.transport.terminal.appointment.appointmentvariance

/** A rule a rolling appointment bundle must pass. Check returns its problems, or none. */
interface RollingAppointmentBundleRule {
    fun check(item: RollingAppointmentBundle): MutableList<String>
}

data class RollingAppointmentBundleCodeLengthRule(var maxLength: Int) : RollingAppointmentBundleRule {
    override fun check(item: RollingAppointmentBundle): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RollingAppointmentBundleRequiredOwnerIdRule(var message: String) : RollingAppointmentBundleRule {
    override fun check(item: RollingAppointmentBundle): MutableList<String> {
        if (item.ownerId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RollingAppointmentBundleRequiredDepotRule(var message: String) : RollingAppointmentBundleRule {
    override fun check(item: RollingAppointmentBundle): MutableList<String> {
        if (item.depot.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RollingAppointmentBundleCodePrefixRule(var prefix: String) : RollingAppointmentBundleRule {
    override fun check(item: RollingAppointmentBundle): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class RollingAppointmentBundleScoreRangeRule(
    var min: Double,
    var max: Double,
) : RollingAppointmentBundleRule {
    override fun check(item: RollingAppointmentBundle): MutableList<String> {
        val value = item.score
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("score $value is below $min")
        }
        if (value > max) {
            problems.add("score $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a rolling appointment bundle. */
data class RollingAppointmentBundleValidator(var rules: MutableList<RollingAppointmentBundleRule>) {
    fun validate(item: RollingAppointmentBundle): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RollingAppointmentBundle) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RollingAppointmentBundle>): MutableMap<String, MutableList<String>> {
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

fun defaultRollingAppointmentBundleValidator(): RollingAppointmentBundleValidator {
    return RollingAppointmentBundleValidator(
        mutableListOf<RollingAppointmentBundleRule>(
            RollingAppointmentBundleCodeLengthRule(16),
            RollingAppointmentBundleRequiredOwnerIdRule("rolling appointment bundle needs an entry"),
            RollingAppointmentBundleRequiredDepotRule("rolling appointment bundle needs this field"),
            RollingAppointmentBundleCodePrefixRule("ACM"),
            RollingAppointmentBundleScoreRangeRule(7.0, 3575.4),
        ),
    )
}
