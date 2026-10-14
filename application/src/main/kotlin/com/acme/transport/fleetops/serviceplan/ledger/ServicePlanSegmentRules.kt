package com.acme.transport.fleetops.serviceplan.ledger

/** A rule a service plan segment must pass. Check returns its problems, or none. */
interface ServicePlanSegmentRule {
    fun check(item: ServicePlanSegment): MutableList<String>
}

data class ServicePlanSegmentRequiredNotesRule(var message: String) : ServicePlanSegmentRule {
    override fun check(item: ServicePlanSegment): MutableList<String> {
        if (item.notes.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ServicePlanSegmentRequiredTitleRule(var message: String) : ServicePlanSegmentRule {
    override fun check(item: ServicePlanSegment): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ServicePlanSegmentGraceDaysRangeRule(var min: Int, var max: Int) : ServicePlanSegmentRule {
    override fun check(item: ServicePlanSegment): MutableList<String> {
        val value = item.graceDays
        if (value < min || value > max) {
            return mutableListOf<String>("grace days $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a service plan segment. */
data class ServicePlanSegmentValidator(var rules: MutableList<ServicePlanSegmentRule>) {
    fun validate(item: ServicePlanSegment): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ServicePlanSegment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ServicePlanSegment>): MutableMap<String, MutableList<String>> {
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

fun defaultServicePlanSegmentValidator(): ServicePlanSegmentValidator {
    return ServicePlanSegmentValidator(
        mutableListOf<ServicePlanSegmentRule>(
            ServicePlanSegmentRequiredNotesRule("service plan segment needs a value"),
            ServicePlanSegmentRequiredTitleRule("service plan segment needs an entry"),
            ServicePlanSegmentGraceDaysRangeRule(80, 255),
        ),
    )
}
