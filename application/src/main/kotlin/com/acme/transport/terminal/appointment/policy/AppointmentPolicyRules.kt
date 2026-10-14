package com.acme.transport.terminal.appointment.policy

/** A rule a appointment policy must pass. Check returns its problems, or none. */
interface AppointmentPolicyRule {
    fun check(item: AppointmentPolicy): MutableList<String>
}

data class AppointmentPolicyShiftCodeLengthRule(var maxLength: Int) : AppointmentPolicyRule {
    override fun check(item: AppointmentPolicy): MutableList<String> {
        val size = item.shiftCode.length
        if (size > maxLength) {
            return mutableListOf<String>("shift code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AppointmentPolicyPalletsRangeRule(var min: Int, var max: Int) : AppointmentPolicyRule {
    override fun check(item: AppointmentPolicy): MutableList<String> {
        val value = item.pallets
        if (value < min || value > max) {
            return mutableListOf<String>("pallets $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class AppointmentPolicyRequiredNameRule(var message: String) : AppointmentPolicyRule {
    override fun check(item: AppointmentPolicy): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a appointment policy. */
data class AppointmentPolicyValidator(var rules: MutableList<AppointmentPolicyRule>) {
    fun validate(item: AppointmentPolicy): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: AppointmentPolicy) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AppointmentPolicy>): MutableMap<String, MutableList<String>> {
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

fun defaultAppointmentPolicyValidator(): AppointmentPolicyValidator {
    return AppointmentPolicyValidator(
        mutableListOf<AppointmentPolicyRule>(
            AppointmentPolicyShiftCodeLengthRule(120),
            AppointmentPolicyPalletsRangeRule(32, 1000),
            AppointmentPolicyRequiredNameRule("appointment policy needs an entry"),
        ),
    )
}
