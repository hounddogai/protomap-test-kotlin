package com.acme.transport.terminal.appointment.appointmentallocation

/** A rule a pending appointment score must pass. Check returns its problems, or none. */
interface PendingAppointmentScoreRule {
    fun check(item: PendingAppointmentScore): MutableList<String>
}

data class PendingAppointmentScoreCodePrefixRule(var prefix: String) : PendingAppointmentScoreRule {
    override fun check(item: PendingAppointmentScore): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class PendingAppointmentScoreGraceDaysRangeRule(var min: Int, var max: Int) : PendingAppointmentScoreRule {
    override fun check(item: PendingAppointmentScore): MutableList<String> {
        val value = item.graceDays
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("grace days $value is below $min")
        }
        if (value > max) {
            problems.add("grace days $value is above $max")
        }
        return problems
    }
}

data class PendingAppointmentScoreUnitLengthRule(var maxLength: Int) : PendingAppointmentScoreRule {
    override fun check(item: PendingAppointmentScore): MutableList<String> {
        val size = item.unit.length
        if (size > maxLength) {
            return mutableListOf<String>("unit is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PendingAppointmentScoreRequiredUnitRule(var message: String) : PendingAppointmentScoreRule {
    override fun check(item: PendingAppointmentScore): MutableList<String> {
        if (item.unit.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PendingAppointmentScoreCodeLengthRule(var maxLength: Int) : PendingAppointmentScoreRule {
    override fun check(item: PendingAppointmentScore): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a pending appointment score. */
data class PendingAppointmentScoreValidator(var rules: MutableList<PendingAppointmentScoreRule>) {
    fun validate(item: PendingAppointmentScore): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: PendingAppointmentScore) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PendingAppointmentScore>): MutableMap<String, MutableList<String>> {
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

fun defaultPendingAppointmentScoreValidator(): PendingAppointmentScoreValidator {
    return PendingAppointmentScoreValidator(
        mutableListOf<PendingAppointmentScoreRule>(
            PendingAppointmentScoreCodePrefixRule("OPS"),
            PendingAppointmentScoreGraceDaysRangeRule(64, 1000),
            PendingAppointmentScoreUnitLengthRule(32),
            PendingAppointmentScoreRequiredUnitRule("pending appointment score needs this field"),
            PendingAppointmentScoreCodeLengthRule(16),
        ),
    )
}
