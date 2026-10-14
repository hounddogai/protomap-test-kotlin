package com.acme.transport.terminal.appointment.score

/** A rule a legacy appointment digest must pass. Check returns its problems, or none. */
interface LegacyAppointmentDigestRule {
    fun check(item: LegacyAppointmentDigest): MutableList<String>
}

data class LegacyAppointmentDigestRequiredSourceRule(var message: String) : LegacyAppointmentDigestRule {
    override fun check(item: LegacyAppointmentDigest): MutableList<String> {
        if (item.source.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LegacyAppointmentDigestExternalRefLengthRule(var maxLength: Int) : LegacyAppointmentDigestRule {
    override fun check(item: LegacyAppointmentDigest): MutableList<String> {
        val size = item.externalRef.length
        if (size > maxLength) {
            return mutableListOf<String>("external ref is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LegacyAppointmentDigestCodeLengthRule(var maxLength: Int) : LegacyAppointmentDigestRule {
    override fun check(item: LegacyAppointmentDigest): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a legacy appointment digest. */
data class LegacyAppointmentDigestValidator(var rules: MutableList<LegacyAppointmentDigestRule>) {
    fun validate(item: LegacyAppointmentDigest): MutableList<String> {
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

    fun requireValid(item: LegacyAppointmentDigest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LegacyAppointmentDigest>): MutableMap<String, MutableList<String>> {
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

fun defaultLegacyAppointmentDigestValidator(): LegacyAppointmentDigestValidator {
    return LegacyAppointmentDigestValidator(
        mutableListOf<LegacyAppointmentDigestRule>(
            LegacyAppointmentDigestRequiredSourceRule("legacy appointment digest needs this field"),
            LegacyAppointmentDigestExternalRefLengthRule(64),
            LegacyAppointmentDigestCodeLengthRule(120),
        ),
    )
}
