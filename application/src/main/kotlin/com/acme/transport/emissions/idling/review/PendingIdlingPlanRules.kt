package com.acme.transport.emissions.idling.review

/** A rule a pending idling plan must pass. Check returns its problems, or none. */
interface PendingIdlingPlanRule {
    fun check(item: PendingIdlingPlan): MutableList<String>
}

data class PendingIdlingPlanUpdatedAtRangeRule(var min: Int, var max: Int) : PendingIdlingPlanRule {
    override fun check(item: PendingIdlingPlan): MutableList<String> {
        val value = item.updatedAt
        if (value < min || value > max) {
            return mutableListOf<String>("updated at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class PendingIdlingPlanDriverIdLengthRule(var maxLength: Int) : PendingIdlingPlanRule {
    override fun check(item: PendingIdlingPlan): MutableList<String> {
        val size = item.driverId.length
        if (size > maxLength) {
            return mutableListOf<String>("driver id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PendingIdlingPlanCountryLengthRule(var maxLength: Int) : PendingIdlingPlanRule {
    override fun check(item: PendingIdlingPlan): MutableList<String> {
        val size = item.country.length
        if (size > maxLength) {
            return mutableListOf<String>("country is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PendingIdlingPlanRequiredCodeRule(var message: String) : PendingIdlingPlanRule {
    override fun check(item: PendingIdlingPlan): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a pending idling plan. */
data class PendingIdlingPlanValidator(var rules: MutableList<PendingIdlingPlanRule>) {
    fun validate(item: PendingIdlingPlan): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: PendingIdlingPlan) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PendingIdlingPlan>): MutableMap<String, MutableList<String>> {
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

fun defaultPendingIdlingPlanValidator(): PendingIdlingPlanValidator {
    return PendingIdlingPlanValidator(
        mutableListOf<PendingIdlingPlanRule>(
            PendingIdlingPlanUpdatedAtRangeRule(3600, 3600),
            PendingIdlingPlanDriverIdLengthRule(80),
            PendingIdlingPlanCountryLengthRule(16),
            PendingIdlingPlanRequiredCodeRule("pending idling plan needs an entry"),
        ),
    )
}
