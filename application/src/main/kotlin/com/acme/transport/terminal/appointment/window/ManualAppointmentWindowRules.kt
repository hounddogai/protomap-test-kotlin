package com.acme.transport.terminal.appointment.window

/** A rule a manual appointment window must pass. Check returns its problems, or none. */
interface ManualAppointmentWindowRule {
    fun check(item: ManualAppointmentWindow): MutableList<String>
}

data class ManualAppointmentWindowRequiredLabelRule(var message: String) : ManualAppointmentWindowRule {
    override fun check(item: ManualAppointmentWindow): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ManualAppointmentWindowMinimumRangeRule(
    var min: Double,
    var max: Double,
) : ManualAppointmentWindowRule {
    override fun check(item: ManualAppointmentWindow): MutableList<String> {
        val value = item.minimum
        if (value < min || value > max) {
            return mutableListOf<String>("minimum $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class ManualAppointmentWindowAttemptsRangeRule(var min: Int, var max: Int) : ManualAppointmentWindowRule {
    override fun check(item: ManualAppointmentWindow): MutableList<String> {
        val value = item.attempts
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("attempts $value is below $min")
        }
        if (value > max) {
            problems.add("attempts $value is above $max")
        }
        return problems
    }
}

data class ManualAppointmentWindowRequiredChannelRule(var message: String) : ManualAppointmentWindowRule {
    override fun check(item: ManualAppointmentWindow): MutableList<String> {
        if (item.channel.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ManualAppointmentWindowChannelLengthRule(var maxLength: Int) : ManualAppointmentWindowRule {
    override fun check(item: ManualAppointmentWindow): MutableList<String> {
        val size = item.channel.length
        if (size > maxLength) {
            return mutableListOf<String>("channel is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ManualAppointmentWindowRequiredShiftCodeRule(var message: String) : ManualAppointmentWindowRule {
    override fun check(item: ManualAppointmentWindow): MutableList<String> {
        if (item.shiftCode.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a manual appointment window. */
data class ManualAppointmentWindowValidator(var rules: MutableList<ManualAppointmentWindowRule>) {
    fun validate(item: ManualAppointmentWindow): MutableList<String> {
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

    fun requireValid(item: ManualAppointmentWindow) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ManualAppointmentWindow>): MutableMap<String, MutableList<String>> {
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

fun defaultManualAppointmentWindowValidator(): ManualAppointmentWindowValidator {
    return ManualAppointmentWindowValidator(
        mutableListOf<ManualAppointmentWindowRule>(
            ManualAppointmentWindowRequiredLabelRule("manual appointment window needs an entry"),
            ManualAppointmentWindowMinimumRangeRule(2.2, 2490.4),
            ManualAppointmentWindowAttemptsRangeRule(1000, 3600),
            ManualAppointmentWindowRequiredChannelRule("manual appointment window needs an entry"),
            ManualAppointmentWindowChannelLengthRule(16),
            ManualAppointmentWindowRequiredShiftCodeRule("manual appointment window needs a value"),
        ),
    )
}
