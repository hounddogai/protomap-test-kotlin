package com.acme.transport.fleetops.serviceplan.planquota

/** A rule a service plan milestone must pass. Check returns its problems, or none. */
interface ServicePlanMilestoneRule {
    fun check(item: ServicePlanMilestone): MutableList<String>
}

data class ServicePlanMilestoneRequiredLocaleRule(var message: String) : ServicePlanMilestoneRule {
    override fun check(item: ServicePlanMilestone): MutableList<String> {
        if (item.locale.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ServicePlanMilestoneAmountRangeRule(var min: Double, var max: Double) : ServicePlanMilestoneRule {
    override fun check(item: ServicePlanMilestone): MutableList<String> {
        val value = item.amount
        if (value < min || value > max) {
            return mutableListOf<String>("amount $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class ServicePlanMilestoneCreatedAtRangeRule(var min: Int, var max: Int) : ServicePlanMilestoneRule {
    override fun check(item: ServicePlanMilestone): MutableList<String> {
        val value = item.createdAt
        if (value < min || value > max) {
            return mutableListOf<String>("created at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class ServicePlanMilestonePinnedGuardRule(var limit: Int) : ServicePlanMilestoneRule {
    override fun check(item: ServicePlanMilestone): MutableList<String> {
        if (item.pinned && item.createdAt > limit) {
            return mutableListOf<String>("created at ${item.createdAt} exceeds $limit while pinned")
        }
        return mutableListOf<String>()
    }
}

data class ServicePlanMilestoneRequiredTitleRule(var message: String) : ServicePlanMilestoneRule {
    override fun check(item: ServicePlanMilestone): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ServicePlanMilestoneRequiredCodeRule(var message: String) : ServicePlanMilestoneRule {
    override fun check(item: ServicePlanMilestone): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a service plan milestone. */
data class ServicePlanMilestoneValidator(var rules: MutableList<ServicePlanMilestoneRule>) {
    fun validate(item: ServicePlanMilestone): MutableList<String> {
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

    fun requireValid(item: ServicePlanMilestone) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ServicePlanMilestone>): MutableMap<String, MutableList<String>> {
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

fun defaultServicePlanMilestoneValidator(): ServicePlanMilestoneValidator {
    return ServicePlanMilestoneValidator(
        mutableListOf<ServicePlanMilestoneRule>(
            ServicePlanMilestoneRequiredLocaleRule("service plan milestone needs a value"),
            ServicePlanMilestoneAmountRangeRule(5.2, 246.0),
            ServicePlanMilestoneCreatedAtRangeRule(64, 3600),
            ServicePlanMilestonePinnedGuardRule(120),
            ServicePlanMilestoneRequiredTitleRule("service plan milestone needs an entry"),
            ServicePlanMilestoneRequiredCodeRule("service plan milestone needs a value"),
        ),
    )
}
