package com.acme.transport.lastmile.redelivery.milestone

/** A rule a redelivery schedule must pass. Check returns its problems, or none. */
interface RedeliveryScheduleRule {
    fun check(item: RedeliverySchedule): MutableList<String>
}

data class RedeliveryScheduleTimezoneLengthRule(var maxLength: Int) : RedeliveryScheduleRule {
    override fun check(item: RedeliverySchedule): MutableList<String> {
        val size = item.timezone.length
        if (size > maxLength) {
            return mutableListOf<String>("timezone is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RedeliveryScheduleRequiredTimezoneRule(var message: String) : RedeliveryScheduleRule {
    override fun check(item: RedeliverySchedule): MutableList<String> {
        if (item.timezone.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RedeliveryScheduleNameLengthRule(var maxLength: Int) : RedeliveryScheduleRule {
    override fun check(item: RedeliverySchedule): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a redelivery schedule. */
data class RedeliveryScheduleValidator(var rules: MutableList<RedeliveryScheduleRule>) {
    fun validate(item: RedeliverySchedule): MutableList<String> {
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

    fun requireValid(item: RedeliverySchedule) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RedeliverySchedule>): MutableMap<String, MutableList<String>> {
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

fun defaultRedeliveryScheduleValidator(): RedeliveryScheduleValidator {
    return RedeliveryScheduleValidator(
        mutableListOf<RedeliveryScheduleRule>(
            RedeliveryScheduleTimezoneLengthRule(120),
            RedeliveryScheduleRequiredTimezoneRule("redelivery schedule needs an entry"),
            RedeliveryScheduleNameLengthRule(32),
        ),
    )
}
