package com.acme.transport.freightpay.audit.template

/** A rule a audit schedule must pass. Check returns its problems, or none. */
interface AuditScheduleRule {
    fun check(item: AuditSchedule): MutableList<String>
}

data class AuditScheduleDepotLengthRule(var maxLength: Int) : AuditScheduleRule {
    override fun check(item: AuditSchedule): MutableList<String> {
        val size = item.depot.length
        if (size > maxLength) {
            return mutableListOf<String>("depot is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AuditScheduleRequiredCodeRule(var message: String) : AuditScheduleRule {
    override fun check(item: AuditSchedule): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AuditScheduleNameLengthRule(var maxLength: Int) : AuditScheduleRule {
    override fun check(item: AuditSchedule): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a audit schedule. */
data class AuditScheduleValidator(var rules: MutableList<AuditScheduleRule>) {
    fun validate(item: AuditSchedule): MutableList<String> {
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

    fun requireValid(item: AuditSchedule) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AuditSchedule>): MutableMap<String, MutableList<String>> {
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

fun defaultAuditScheduleValidator(): AuditScheduleValidator {
    return AuditScheduleValidator(
        mutableListOf<AuditScheduleRule>(
            AuditScheduleDepotLengthRule(80),
            AuditScheduleRequiredCodeRule("audit schedule needs an entry"),
            AuditScheduleNameLengthRule(1000),
        ),
    )
}
