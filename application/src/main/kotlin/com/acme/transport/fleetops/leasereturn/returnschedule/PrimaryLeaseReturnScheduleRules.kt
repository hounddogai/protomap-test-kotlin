package com.acme.transport.fleetops.leasereturn.returnschedule

/** A rule a primary lease return schedule must pass. Check returns its problems, or none. */
interface PrimaryLeaseReturnScheduleRule {
    fun check(item: PrimaryLeaseReturnSchedule): MutableList<String>
}

data class PrimaryLeaseReturnScheduleTimezoneLengthRule(var maxLength: Int) : PrimaryLeaseReturnScheduleRule {
    override fun check(item: PrimaryLeaseReturnSchedule): MutableList<String> {
        val size = item.timezone.length
        if (size > maxLength) {
            return mutableListOf<String>("timezone is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryLeaseReturnScheduleCodeLengthRule(var maxLength: Int) : PrimaryLeaseReturnScheduleRule {
    override fun check(item: PrimaryLeaseReturnSchedule): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryLeaseReturnScheduleRequiredSourceRule(var message: String) : PrimaryLeaseReturnScheduleRule {
    override fun check(item: PrimaryLeaseReturnSchedule): MutableList<String> {
        if (item.source.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PrimaryLeaseReturnScheduleLabelLengthRule(var maxLength: Int) : PrimaryLeaseReturnScheduleRule {
    override fun check(item: PrimaryLeaseReturnSchedule): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a primary lease return schedule. */
data class PrimaryLeaseReturnScheduleValidator(var rules: MutableList<PrimaryLeaseReturnScheduleRule>) {
    fun validate(item: PrimaryLeaseReturnSchedule): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: PrimaryLeaseReturnSchedule) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PrimaryLeaseReturnSchedule>): MutableMap<String, MutableList<String>> {
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

fun defaultPrimaryLeaseReturnScheduleValidator(): PrimaryLeaseReturnScheduleValidator {
    return PrimaryLeaseReturnScheduleValidator(
        mutableListOf<PrimaryLeaseReturnScheduleRule>(
            PrimaryLeaseReturnScheduleTimezoneLengthRule(1000),
            PrimaryLeaseReturnScheduleCodeLengthRule(64),
            PrimaryLeaseReturnScheduleRequiredSourceRule("primary lease return schedule needs this field"),
            PrimaryLeaseReturnScheduleLabelLengthRule(16),
        ),
    )
}
