package com.acme.transport.safety.incident.incidentreservation

/** A rule a legacy incident reservation must pass. Check returns its problems, or none. */
interface LegacyIncidentReservationRule {
    fun check(item: LegacyIncidentReservation): MutableList<String>
}

data class LegacyIncidentReservationActiveGuardRule(var limit: Int) : LegacyIncidentReservationRule {
    override fun check(item: LegacyIncidentReservation): MutableList<String> {
        if (item.active && item.etaMinutes > limit) {
            return mutableListOf<String>("eta minutes ${item.etaMinutes} exceeds $limit while active")
        }
        return mutableListOf<String>()
    }
}

data class LegacyIncidentReservationRequiredCodeRule(var message: String) : LegacyIncidentReservationRule {
    override fun check(item: LegacyIncidentReservation): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LegacyIncidentReservationRequiredNameRule(var message: String) : LegacyIncidentReservationRule {
    override fun check(item: LegacyIncidentReservation): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LegacyIncidentReservationCodeLengthRule(var maxLength: Int) : LegacyIncidentReservationRule {
    override fun check(item: LegacyIncidentReservation): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LegacyIncidentReservationCodePrefixRule(var prefix: String) : LegacyIncidentReservationRule {
    override fun check(item: LegacyIncidentReservation): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class LegacyIncidentReservationRequiredNotesRule(var message: String) : LegacyIncidentReservationRule {
    override fun check(item: LegacyIncidentReservation): MutableList<String> {
        if (item.notes.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a legacy incident reservation. */
data class LegacyIncidentReservationValidator(var rules: MutableList<LegacyIncidentReservationRule>) {
    fun validate(item: LegacyIncidentReservation): MutableList<String> {
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

    fun requireValid(item: LegacyIncidentReservation) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LegacyIncidentReservation>): MutableMap<String, MutableList<String>> {
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

fun defaultLegacyIncidentReservationValidator(): LegacyIncidentReservationValidator {
    return LegacyIncidentReservationValidator(
        mutableListOf<LegacyIncidentReservationRule>(
            LegacyIncidentReservationActiveGuardRule(86400),
            LegacyIncidentReservationRequiredCodeRule("legacy incident reservation needs a value"),
            LegacyIncidentReservationRequiredNameRule("legacy incident reservation needs this field"),
            LegacyIncidentReservationCodeLengthRule(120),
            LegacyIncidentReservationCodePrefixRule("REF"),
            LegacyIncidentReservationRequiredNotesRule("legacy incident reservation needs a value"),
        ),
    )
}
