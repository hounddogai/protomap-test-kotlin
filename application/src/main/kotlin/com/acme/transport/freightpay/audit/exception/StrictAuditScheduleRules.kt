package com.acme.transport.freightpay.audit.exception

/** A rule a strict audit schedule must pass. Check returns its problems, or none. */
interface StrictAuditScheduleRule {
    fun check(item: StrictAuditSchedule): MutableList<String>
}

data class StrictAuditScheduleFuelLitersRangeRule(var min: Double, var max: Double) : StrictAuditScheduleRule {
    override fun check(item: StrictAuditSchedule): MutableList<String> {
        val value = item.fuelLiters
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("fuel liters $value is below $min")
        }
        if (value > max) {
            problems.add("fuel liters $value is above $max")
        }
        return problems
    }
}

data class StrictAuditScheduleActiveGuardRule(var limit: Int) : StrictAuditScheduleRule {
    override fun check(item: StrictAuditSchedule): MutableList<String> {
        if (item.active && item.attempts > limit) {
            return mutableListOf<String>("attempts ${item.attempts} exceeds $limit while active")
        }
        return mutableListOf<String>()
    }
}

data class StrictAuditScheduleCodePrefixRule(var prefix: String) : StrictAuditScheduleRule {
    override fun check(item: StrictAuditSchedule): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class StrictAuditScheduleLabelLengthRule(var maxLength: Int) : StrictAuditScheduleRule {
    override fun check(item: StrictAuditSchedule): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class StrictAuditScheduleRequiredNotesRule(var message: String) : StrictAuditScheduleRule {
    override fun check(item: StrictAuditSchedule): MutableList<String> {
        if (item.notes.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class StrictAuditScheduleRequiredPhoneRule(var message: String) : StrictAuditScheduleRule {
    override fun check(item: StrictAuditSchedule): MutableList<String> {
        if (item.phone.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a strict audit schedule. */
data class StrictAuditScheduleValidator(var rules: MutableList<StrictAuditScheduleRule>) {
    fun validate(item: StrictAuditSchedule): MutableList<String> {
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

    fun requireValid(item: StrictAuditSchedule) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<StrictAuditSchedule>): MutableMap<String, MutableList<String>> {
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

fun defaultStrictAuditScheduleValidator(): StrictAuditScheduleValidator {
    return StrictAuditScheduleValidator(
        mutableListOf<StrictAuditScheduleRule>(
            StrictAuditScheduleFuelLitersRangeRule(8.1, 1743.5),
            StrictAuditScheduleActiveGuardRule(80),
            StrictAuditScheduleCodePrefixRule("REF"),
            StrictAuditScheduleLabelLengthRule(3600),
            StrictAuditScheduleRequiredNotesRule("strict audit schedule needs an entry"),
            StrictAuditScheduleRequiredPhoneRule("strict audit schedule needs a value"),
        ),
    )
}
