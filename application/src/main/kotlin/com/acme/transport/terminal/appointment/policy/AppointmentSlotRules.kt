package com.acme.transport.terminal.appointment.policy

/** A rule a appointment slot must pass. Check returns its problems, or none. */
interface AppointmentSlotRule {
    fun check(item: AppointmentSlot): MutableList<String>
}

data class AppointmentSlotRequiredTitleRule(var message: String) : AppointmentSlotRule {
    override fun check(item: AppointmentSlot): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AppointmentSlotTitleLengthRule(var maxLength: Int) : AppointmentSlotRule {
    override fun check(item: AppointmentSlot): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AppointmentSlotRequiredCurrencyRule(var message: String) : AppointmentSlotRule {
    override fun check(item: AppointmentSlot): MutableList<String> {
        if (item.currency.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a appointment slot. */
data class AppointmentSlotValidator(var rules: MutableList<AppointmentSlotRule>) {
    fun validate(item: AppointmentSlot): MutableList<String> {
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

    fun requireValid(item: AppointmentSlot) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AppointmentSlot>): MutableMap<String, MutableList<String>> {
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

fun defaultAppointmentSlotValidator(): AppointmentSlotValidator {
    return AppointmentSlotValidator(
        mutableListOf<AppointmentSlotRule>(
            AppointmentSlotRequiredTitleRule("appointment slot needs an entry"),
            AppointmentSlotTitleLengthRule(80),
            AppointmentSlotRequiredCurrencyRule("appointment slot needs a value"),
        ),
    )
}
