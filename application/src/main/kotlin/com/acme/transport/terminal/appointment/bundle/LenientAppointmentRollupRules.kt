package com.acme.transport.terminal.appointment.bundle

/** A rule a lenient appointment rollup must pass. Check returns its problems, or none. */
interface LenientAppointmentRollupRule {
    fun check(item: LenientAppointmentRollup): MutableList<String>
}

data class LenientAppointmentRollupRequiredSummaryRule(var message: String) : LenientAppointmentRollupRule {
    override fun check(item: LenientAppointmentRollup): MutableList<String> {
        if (item.summary.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LenientAppointmentRollupCodePrefixRule(var prefix: String) : LenientAppointmentRollupRule {
    override fun check(item: LenientAppointmentRollup): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class LenientAppointmentRollupCodeLengthRule(var maxLength: Int) : LenientAppointmentRollupRule {
    override fun check(item: LenientAppointmentRollup): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LenientAppointmentRollupLabelLengthRule(var maxLength: Int) : LenientAppointmentRollupRule {
    override fun check(item: LenientAppointmentRollup): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a lenient appointment rollup. */
data class LenientAppointmentRollupValidator(var rules: MutableList<LenientAppointmentRollupRule>) {
    fun validate(item: LenientAppointmentRollup): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: LenientAppointmentRollup) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LenientAppointmentRollup>): MutableMap<String, MutableList<String>> {
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

fun defaultLenientAppointmentRollupValidator(): LenientAppointmentRollupValidator {
    return LenientAppointmentRollupValidator(
        mutableListOf<LenientAppointmentRollupRule>(
            LenientAppointmentRollupRequiredSummaryRule("lenient appointment rollup needs this field"),
            LenientAppointmentRollupCodePrefixRule("REF"),
            LenientAppointmentRollupCodeLengthRule(3600),
            LenientAppointmentRollupLabelLengthRule(255),
        ),
    )
}
