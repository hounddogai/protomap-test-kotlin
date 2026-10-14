package com.acme.transport.terminal.appointment.appointmenttemplate

/** A rule a capped appointment template must pass. Check returns its problems, or none. */
interface CappedAppointmentTemplateRule {
    fun check(item: CappedAppointmentTemplate): MutableList<String>
}

data class CappedAppointmentTemplateLimitRangeRule(var min: Int, var max: Int) : CappedAppointmentTemplateRule {
    override fun check(item: CappedAppointmentTemplate): MutableList<String> {
        val value = item.limit
        if (value < min || value > max) {
            return mutableListOf<String>("limit $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class CappedAppointmentTemplateLabelLengthRule(var maxLength: Int) : CappedAppointmentTemplateRule {
    override fun check(item: CappedAppointmentTemplate): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CappedAppointmentTemplateActiveGuardRule(var limit: Int) : CappedAppointmentTemplateRule {
    override fun check(item: CappedAppointmentTemplate): MutableList<String> {
        if (item.active && item.limit > limit) {
            return mutableListOf<String>("limit ${item.limit} exceeds $limit while active")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a capped appointment template. */
data class CappedAppointmentTemplateValidator(var rules: MutableList<CappedAppointmentTemplateRule>) {
    fun validate(item: CappedAppointmentTemplate): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: CappedAppointmentTemplate) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CappedAppointmentTemplate>): MutableMap<String, MutableList<String>> {
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

fun defaultCappedAppointmentTemplateValidator(): CappedAppointmentTemplateValidator {
    return CappedAppointmentTemplateValidator(
        mutableListOf<CappedAppointmentTemplateRule>(
            CappedAppointmentTemplateLimitRangeRule(64, 86400),
            CappedAppointmentTemplateLabelLengthRule(80),
            CappedAppointmentTemplateActiveGuardRule(32),
        ),
    )
}
