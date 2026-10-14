package com.acme.transport.terminal.appointment.rollup

/** A rule a appointment notice must pass. Check returns its problems, or none. */
interface AppointmentNoticeRule {
    fun check(item: AppointmentNotice): MutableList<String>
}

data class AppointmentNoticeCodeLengthRule(var maxLength: Int) : AppointmentNoticeRule {
    override fun check(item: AppointmentNotice): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AppointmentNoticeNameLengthRule(var maxLength: Int) : AppointmentNoticeRule {
    override fun check(item: AppointmentNotice): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AppointmentNoticeRequiredDepotRule(var message: String) : AppointmentNoticeRule {
    override fun check(item: AppointmentNotice): MutableList<String> {
        if (item.depot.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AppointmentNoticeQuantityRangeRule(var min: Int, var max: Int) : AppointmentNoticeRule {
    override fun check(item: AppointmentNotice): MutableList<String> {
        val value = item.quantity
        if (value < min || value > max) {
            return mutableListOf<String>("quantity $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class AppointmentNoticeRequiredNameRule(var message: String) : AppointmentNoticeRule {
    override fun check(item: AppointmentNotice): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a appointment notice. */
data class AppointmentNoticeValidator(var rules: MutableList<AppointmentNoticeRule>) {
    fun validate(item: AppointmentNotice): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: AppointmentNotice) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AppointmentNotice>): MutableMap<String, MutableList<String>> {
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

fun defaultAppointmentNoticeValidator(): AppointmentNoticeValidator {
    return AppointmentNoticeValidator(
        mutableListOf<AppointmentNoticeRule>(
            AppointmentNoticeCodeLengthRule(255),
            AppointmentNoticeNameLengthRule(64),
            AppointmentNoticeRequiredDepotRule("appointment notice needs this field"),
            AppointmentNoticeQuantityRangeRule(16, 1000),
            AppointmentNoticeRequiredNameRule("appointment notice needs this field"),
        ),
    )
}
