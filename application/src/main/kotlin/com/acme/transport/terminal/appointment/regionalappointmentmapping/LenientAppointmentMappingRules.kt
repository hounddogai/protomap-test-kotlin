package com.acme.transport.terminal.appointment.regionalappointmentmapping

/** A rule a lenient appointment mapping must pass. Check returns its problems, or none. */
interface LenientAppointmentMappingRule {
    fun check(item: LenientAppointmentMapping): MutableList<String>
}

data class LenientAppointmentMappingEtaMinutesRangeRule(
    var min: Int,
    var max: Int,
) : LenientAppointmentMappingRule {
    override fun check(item: LenientAppointmentMapping): MutableList<String> {
        val value = item.etaMinutes
        if (value < min || value > max) {
            return mutableListOf<String>("eta minutes $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class LenientAppointmentMappingCodePrefixRule(var prefix: String) : LenientAppointmentMappingRule {
    override fun check(item: LenientAppointmentMapping): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class LenientAppointmentMappingRetriesRangeRule(
    var min: Int,
    var max: Int,
) : LenientAppointmentMappingRule {
    override fun check(item: LenientAppointmentMapping): MutableList<String> {
        val value = item.retries
        if (value < min || value > max) {
            return mutableListOf<String>("retries $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class LenientAppointmentMappingTitleLengthRule(var maxLength: Int) : LenientAppointmentMappingRule {
    override fun check(item: LenientAppointmentMapping): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LenientAppointmentMappingRequiredChecksumRule(var message: String) : LenientAppointmentMappingRule {
    override fun check(item: LenientAppointmentMapping): MutableList<String> {
        if (item.checksum.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a lenient appointment mapping. */
data class LenientAppointmentMappingValidator(var rules: MutableList<LenientAppointmentMappingRule>) {
    fun validate(item: LenientAppointmentMapping): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: LenientAppointmentMapping) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LenientAppointmentMapping>): MutableMap<String, MutableList<String>> {
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

fun defaultLenientAppointmentMappingValidator(): LenientAppointmentMappingValidator {
    return LenientAppointmentMappingValidator(
        mutableListOf<LenientAppointmentMappingRule>(
            LenientAppointmentMappingEtaMinutesRangeRule(16, 80),
            LenientAppointmentMappingCodePrefixRule("Q"),
            LenientAppointmentMappingRetriesRangeRule(64, 120),
            LenientAppointmentMappingTitleLengthRule(32),
            LenientAppointmentMappingRequiredChecksumRule("lenient appointment mapping needs this field"),
        ),
    )
}
