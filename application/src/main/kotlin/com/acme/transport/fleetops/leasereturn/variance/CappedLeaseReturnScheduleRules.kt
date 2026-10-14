package com.acme.transport.fleetops.leasereturn.variance

/** A rule a capped lease return schedule must pass. Check returns its problems, or none. */
interface CappedLeaseReturnScheduleRule {
    fun check(item: CappedLeaseReturnSchedule): MutableList<String>
}

data class CappedLeaseReturnScheduleCodeLengthRule(var maxLength: Int) : CappedLeaseReturnScheduleRule {
    override fun check(item: CappedLeaseReturnSchedule): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CappedLeaseReturnScheduleRequiredNotesRule(var message: String) : CappedLeaseReturnScheduleRule {
    override fun check(item: CappedLeaseReturnSchedule): MutableList<String> {
        if (item.notes.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CappedLeaseReturnScheduleNotesLengthRule(var maxLength: Int) : CappedLeaseReturnScheduleRule {
    override fun check(item: CappedLeaseReturnSchedule): MutableList<String> {
        val size = item.notes.length
        if (size > maxLength) {
            return mutableListOf<String>("notes is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CappedLeaseReturnScheduleRequiredLabelRule(var message: String) : CappedLeaseReturnScheduleRule {
    override fun check(item: CappedLeaseReturnSchedule): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a capped lease return schedule. */
data class CappedLeaseReturnScheduleValidator(var rules: MutableList<CappedLeaseReturnScheduleRule>) {
    fun validate(item: CappedLeaseReturnSchedule): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: CappedLeaseReturnSchedule) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CappedLeaseReturnSchedule>): MutableMap<String, MutableList<String>> {
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

fun defaultCappedLeaseReturnScheduleValidator(): CappedLeaseReturnScheduleValidator {
    return CappedLeaseReturnScheduleValidator(
        mutableListOf<CappedLeaseReturnScheduleRule>(
            CappedLeaseReturnScheduleCodeLengthRule(64),
            CappedLeaseReturnScheduleRequiredNotesRule("capped lease return schedule needs an entry"),
            CappedLeaseReturnScheduleNotesLengthRule(64),
            CappedLeaseReturnScheduleRequiredLabelRule("capped lease return schedule needs an entry"),
        ),
    )
}
