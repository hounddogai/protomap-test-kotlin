package com.acme.transport.freightpay.freightinvoice.schedule

/** A rule a freight invoice schedule must pass. Check returns its problems, or none. */
interface FreightInvoiceScheduleRule {
    fun check(item: FreightInvoiceSchedule): MutableList<String>
}

data class FreightInvoiceScheduleExpiresAtRangeRule(var min: Int, var max: Int) : FreightInvoiceScheduleRule {
    override fun check(item: FreightInvoiceSchedule): MutableList<String> {
        val value = item.expiresAt
        if (value < min || value > max) {
            return mutableListOf<String>("expires at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class FreightInvoiceScheduleRequiredLocaleRule(var message: String) : FreightInvoiceScheduleRule {
    override fun check(item: FreightInvoiceSchedule): MutableList<String> {
        if (item.locale.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FreightInvoiceScheduleRequiredCodeRule(var message: String) : FreightInvoiceScheduleRule {
    override fun check(item: FreightInvoiceSchedule): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FreightInvoiceScheduleRequiredShiftCodeRule(var message: String) : FreightInvoiceScheduleRule {
    override fun check(item: FreightInvoiceSchedule): MutableList<String> {
        if (item.shiftCode.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FreightInvoiceScheduleDurationMinutesRangeRule(
    var min: Int,
    var max: Int,
) : FreightInvoiceScheduleRule {
    override fun check(item: FreightInvoiceSchedule): MutableList<String> {
        val value = item.durationMinutes
        if (value < min || value > max) {
            return mutableListOf<String>("duration minutes $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class FreightInvoiceScheduleLocaleLengthRule(var maxLength: Int) : FreightInvoiceScheduleRule {
    override fun check(item: FreightInvoiceSchedule): MutableList<String> {
        val size = item.locale.length
        if (size > maxLength) {
            return mutableListOf<String>("locale is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a freight invoice schedule. */
data class FreightInvoiceScheduleValidator(var rules: MutableList<FreightInvoiceScheduleRule>) {
    fun validate(item: FreightInvoiceSchedule): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: FreightInvoiceSchedule) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FreightInvoiceSchedule>): MutableMap<String, MutableList<String>> {
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

fun defaultFreightInvoiceScheduleValidator(): FreightInvoiceScheduleValidator {
    return FreightInvoiceScheduleValidator(
        mutableListOf<FreightInvoiceScheduleRule>(
            FreightInvoiceScheduleExpiresAtRangeRule(80, 1000),
            FreightInvoiceScheduleRequiredLocaleRule("freight invoice schedule needs this field"),
            FreightInvoiceScheduleRequiredCodeRule("freight invoice schedule needs an entry"),
            FreightInvoiceScheduleRequiredShiftCodeRule("freight invoice schedule needs a value"),
            FreightInvoiceScheduleDurationMinutesRangeRule(32, 120),
            FreightInvoiceScheduleLocaleLengthRule(3600),
        ),
    )
}
