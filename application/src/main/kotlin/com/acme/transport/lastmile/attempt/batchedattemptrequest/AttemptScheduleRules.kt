package com.acme.transport.lastmile.attempt.batchedattemptrequest

/** A rule a attempt schedule must pass. Check returns its problems, or none. */
interface AttemptScheduleRule {
    fun check(item: AttemptSchedule): MutableList<String>
}

data class AttemptScheduleRequiredNameRule(var message: String) : AttemptScheduleRule {
    override fun check(item: AttemptSchedule): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AttemptScheduleNameLengthRule(var maxLength: Int) : AttemptScheduleRule {
    override fun check(item: AttemptSchedule): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AttemptScheduleSummaryLengthRule(var maxLength: Int) : AttemptScheduleRule {
    override fun check(item: AttemptSchedule): MutableList<String> {
        val size = item.summary.length
        if (size > maxLength) {
            return mutableListOf<String>("summary is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AttemptScheduleRequiredCodeRule(var message: String) : AttemptScheduleRule {
    override fun check(item: AttemptSchedule): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a attempt schedule. */
data class AttemptScheduleValidator(var rules: MutableList<AttemptScheduleRule>) {
    fun validate(item: AttemptSchedule): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: AttemptSchedule) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AttemptSchedule>): MutableMap<String, MutableList<String>> {
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

fun defaultAttemptScheduleValidator(): AttemptScheduleValidator {
    return AttemptScheduleValidator(
        mutableListOf<AttemptScheduleRule>(
            AttemptScheduleRequiredNameRule("attempt schedule needs an entry"),
            AttemptScheduleNameLengthRule(255),
            AttemptScheduleSummaryLengthRule(120),
            AttemptScheduleRequiredCodeRule("attempt schedule needs this field"),
        ),
    )
}
