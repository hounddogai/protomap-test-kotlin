package com.acme.transport.safety.incident.defaultincidentplan

/** A rule a adaptive incident plan must pass. Check returns its problems, or none. */
interface AdaptiveIncidentPlanRule {
    fun check(item: AdaptiveIncidentPlan): MutableList<String>
}

data class AdaptiveIncidentPlanPinnedGuardRule(var limit: Int) : AdaptiveIncidentPlanRule {
    override fun check(item: AdaptiveIncidentPlan): MutableList<String> {
        if (item.pinned && item.capacity > limit) {
            return mutableListOf<String>("capacity ${item.capacity} exceeds $limit while pinned")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveIncidentPlanRequiredDepotRule(var message: String) : AdaptiveIncidentPlanRule {
    override fun check(item: AdaptiveIncidentPlan): MutableList<String> {
        if (item.depot.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveIncidentPlanDepotLengthRule(var maxLength: Int) : AdaptiveIncidentPlanRule {
    override fun check(item: AdaptiveIncidentPlan): MutableList<String> {
        val size = item.depot.length
        if (size > maxLength) {
            return mutableListOf<String>("depot is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a adaptive incident plan. */
data class AdaptiveIncidentPlanValidator(var rules: MutableList<AdaptiveIncidentPlanRule>) {
    fun validate(item: AdaptiveIncidentPlan): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: AdaptiveIncidentPlan) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AdaptiveIncidentPlan>): MutableMap<String, MutableList<String>> {
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

fun defaultAdaptiveIncidentPlanValidator(): AdaptiveIncidentPlanValidator {
    return AdaptiveIncidentPlanValidator(
        mutableListOf<AdaptiveIncidentPlanRule>(
            AdaptiveIncidentPlanPinnedGuardRule(3600),
            AdaptiveIncidentPlanRequiredDepotRule("adaptive incident plan needs a value"),
            AdaptiveIncidentPlanDepotLengthRule(64),
        ),
    )
}
