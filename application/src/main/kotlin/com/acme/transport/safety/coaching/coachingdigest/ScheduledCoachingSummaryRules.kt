package com.acme.transport.safety.coaching.coachingdigest

/** A rule a scheduled coaching summary must pass. Check returns its problems, or none. */
interface ScheduledCoachingSummaryRule {
    fun check(item: ScheduledCoachingSummary): MutableList<String>
}

data class ScheduledCoachingSummaryCodePrefixRule(var prefix: String) : ScheduledCoachingSummaryRule {
    override fun check(item: ScheduledCoachingSummary): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledCoachingSummaryRequiredNameRule(var message: String) : ScheduledCoachingSummaryRule {
    override fun check(item: ScheduledCoachingSummary): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ScheduledCoachingSummaryRequiredRouteCodeRule(var message: String) : ScheduledCoachingSummaryRule {
    override fun check(item: ScheduledCoachingSummary): MutableList<String> {
        if (item.routeCode.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ScheduledCoachingSummaryPriorityRangeRule(
    var min: Int,
    var max: Int,
) : ScheduledCoachingSummaryRule {
    override fun check(item: ScheduledCoachingSummary): MutableList<String> {
        val value = item.priority
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("priority $value is below $min")
        }
        if (value > max) {
            problems.add("priority $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a scheduled coaching summary. */
data class ScheduledCoachingSummaryValidator(var rules: MutableList<ScheduledCoachingSummaryRule>) {
    fun validate(item: ScheduledCoachingSummary): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ScheduledCoachingSummary) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ScheduledCoachingSummary>): MutableMap<String, MutableList<String>> {
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

fun defaultScheduledCoachingSummaryValidator(): ScheduledCoachingSummaryValidator {
    return ScheduledCoachingSummaryValidator(
        mutableListOf<ScheduledCoachingSummaryRule>(
            ScheduledCoachingSummaryCodePrefixRule("Q"),
            ScheduledCoachingSummaryRequiredNameRule("scheduled coaching summary needs an entry"),
            ScheduledCoachingSummaryRequiredRouteCodeRule("scheduled coaching summary needs an entry"),
            ScheduledCoachingSummaryPriorityRangeRule(120, 3600),
        ),
    )
}
