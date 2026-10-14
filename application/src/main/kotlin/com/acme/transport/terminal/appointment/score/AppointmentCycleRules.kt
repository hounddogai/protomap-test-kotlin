package com.acme.transport.terminal.appointment.score

/** A rule a appointment cycle must pass. Check returns its problems, or none. */
interface AppointmentCycleRule {
    fun check(item: AppointmentCycle): MutableList<String>
}

data class AppointmentCycleSourceLengthRule(var maxLength: Int) : AppointmentCycleRule {
    override fun check(item: AppointmentCycle): MutableList<String> {
        val size = item.source.length
        if (size > maxLength) {
            return mutableListOf<String>("source is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AppointmentCycleRequiredSourceRule(var message: String) : AppointmentCycleRule {
    override fun check(item: AppointmentCycle): MutableList<String> {
        if (item.source.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AppointmentCyclePalletsRangeRule(var min: Int, var max: Int) : AppointmentCycleRule {
    override fun check(item: AppointmentCycle): MutableList<String> {
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

data class AppointmentCycleRequiredContactEmailRule(var message: String) : AppointmentCycleRule {
    override fun check(item: AppointmentCycle): MutableList<String> {
        if (item.contactEmail.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AppointmentCycleContactEmailLengthRule(var maxLength: Int) : AppointmentCycleRule {
    override fun check(item: AppointmentCycle): MutableList<String> {
        val size = item.contactEmail.length
        if (size > maxLength) {
            return mutableListOf<String>("contact email is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AppointmentCycleRequiredCodeRule(var message: String) : AppointmentCycleRule {
    override fun check(item: AppointmentCycle): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a appointment cycle. */
data class AppointmentCycleValidator(var rules: MutableList<AppointmentCycleRule>) {
    fun validate(item: AppointmentCycle): MutableList<String> {
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

    fun requireValid(item: AppointmentCycle) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AppointmentCycle>): MutableMap<String, MutableList<String>> {
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

fun defaultAppointmentCycleValidator(): AppointmentCycleValidator {
    return AppointmentCycleValidator(
        mutableListOf<AppointmentCycleRule>(
            AppointmentCycleSourceLengthRule(86400),
            AppointmentCycleRequiredSourceRule("appointment cycle needs a value"),
            AppointmentCyclePalletsRangeRule(32, 64),
            AppointmentCycleRequiredContactEmailRule("appointment cycle needs an entry"),
            AppointmentCycleContactEmailLengthRule(120),
            AppointmentCycleRequiredCodeRule("appointment cycle needs an entry"),
        ),
    )
}
