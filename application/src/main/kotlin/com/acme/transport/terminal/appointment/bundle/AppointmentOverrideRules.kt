package com.acme.transport.terminal.appointment.bundle

/** A rule a appointment override must pass. Check returns its problems, or none. */
interface AppointmentOverrideRule {
    fun check(item: AppointmentOverride): MutableList<String>
}

data class AppointmentOverrideBatchSizeRangeRule(var min: Int, var max: Int) : AppointmentOverrideRule {
    override fun check(item: AppointmentOverride): MutableList<String> {
        val value = item.batchSize
        if (value < min || value > max) {
            return mutableListOf<String>("batch size $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class AppointmentOverrideCodeLengthRule(var maxLength: Int) : AppointmentOverrideRule {
    override fun check(item: AppointmentOverride): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AppointmentOverrideRequiredCodeRule(var message: String) : AppointmentOverrideRule {
    override fun check(item: AppointmentOverride): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AppointmentOverrideFuelLitersRangeRule(var min: Double, var max: Double) : AppointmentOverrideRule {
    override fun check(item: AppointmentOverride): MutableList<String> {
        val value = item.fuelLiters
        if (value < min || value > max) {
            return mutableListOf<String>("fuel liters $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class AppointmentOverrideRequiredNameRule(var message: String) : AppointmentOverrideRule {
    override fun check(item: AppointmentOverride): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a appointment override. */
data class AppointmentOverrideValidator(var rules: MutableList<AppointmentOverrideRule>) {
    fun validate(item: AppointmentOverride): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: AppointmentOverride) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AppointmentOverride>): MutableMap<String, MutableList<String>> {
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

fun defaultAppointmentOverrideValidator(): AppointmentOverrideValidator {
    return AppointmentOverrideValidator(
        mutableListOf<AppointmentOverrideRule>(
            AppointmentOverrideBatchSizeRangeRule(64, 80),
            AppointmentOverrideCodeLengthRule(64),
            AppointmentOverrideRequiredCodeRule("appointment override needs an entry"),
            AppointmentOverrideFuelLitersRangeRule(4.4, 1550.6),
            AppointmentOverrideRequiredNameRule("appointment override needs this field"),
        ),
    )
}
