package com.acme.transport.terminal.appointment.appointmentscore

/** A rule a tiered appointment claim must pass. Check returns its problems, or none. */
interface TieredAppointmentClaimRule {
    fun check(item: TieredAppointmentClaim): MutableList<String>
}

data class TieredAppointmentClaimRequiredCodeRule(var message: String) : TieredAppointmentClaimRule {
    override fun check(item: TieredAppointmentClaim): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TieredAppointmentClaimRequiredLabelRule(var message: String) : TieredAppointmentClaimRule {
    override fun check(item: TieredAppointmentClaim): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TieredAppointmentClaimDurationMinutesRangeRule(
    var min: Int,
    var max: Int,
) : TieredAppointmentClaimRule {
    override fun check(item: TieredAppointmentClaim): MutableList<String> {
        val value = item.durationMinutes
        if (value < min || value > max) {
            return mutableListOf<String>("duration minutes $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class TieredAppointmentClaimCodeLengthRule(var maxLength: Int) : TieredAppointmentClaimRule {
    override fun check(item: TieredAppointmentClaim): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TieredAppointmentClaimLabelLengthRule(var maxLength: Int) : TieredAppointmentClaimRule {
    override fun check(item: TieredAppointmentClaim): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TieredAppointmentClaimTitleLengthRule(var maxLength: Int) : TieredAppointmentClaimRule {
    override fun check(item: TieredAppointmentClaim): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a tiered appointment claim. */
data class TieredAppointmentClaimValidator(var rules: MutableList<TieredAppointmentClaimRule>) {
    fun validate(item: TieredAppointmentClaim): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: TieredAppointmentClaim) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TieredAppointmentClaim>): MutableMap<String, MutableList<String>> {
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

fun defaultTieredAppointmentClaimValidator(): TieredAppointmentClaimValidator {
    return TieredAppointmentClaimValidator(
        mutableListOf<TieredAppointmentClaimRule>(
            TieredAppointmentClaimRequiredCodeRule("tiered appointment claim needs a value"),
            TieredAppointmentClaimRequiredLabelRule("tiered appointment claim needs a value"),
            TieredAppointmentClaimDurationMinutesRangeRule(86400, 86400),
            TieredAppointmentClaimCodeLengthRule(120),
            TieredAppointmentClaimLabelLengthRule(3600),
            TieredAppointmentClaimTitleLengthRule(86400),
        ),
    )
}
