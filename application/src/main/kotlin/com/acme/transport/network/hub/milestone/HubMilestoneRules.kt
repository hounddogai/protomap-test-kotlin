package com.acme.transport.network.hub.milestone

/** A rule a hub milestone must pass. Check returns its problems, or none. */
interface HubMilestoneRule {
    fun check(item: HubMilestone): MutableList<String>
}

data class HubMilestonePalletsRangeRule(var min: Int, var max: Int) : HubMilestoneRule {
    override fun check(item: HubMilestone): MutableList<String> {
        val value = item.pallets
        if (value < min || value > max) {
            return mutableListOf<String>("pallets $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class HubMilestoneRequiredRouteCodeRule(var message: String) : HubMilestoneRule {
    override fun check(item: HubMilestone): MutableList<String> {
        if (item.routeCode.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class HubMilestoneTitleLengthRule(var maxLength: Int) : HubMilestoneRule {
    override fun check(item: HubMilestone): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a hub milestone. */
data class HubMilestoneValidator(var rules: MutableList<HubMilestoneRule>) {
    fun validate(item: HubMilestone): MutableList<String> {
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

    fun requireValid(item: HubMilestone) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<HubMilestone>): MutableMap<String, MutableList<String>> {
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

fun defaultHubMilestoneValidator(): HubMilestoneValidator {
    return HubMilestoneValidator(
        mutableListOf<HubMilestoneRule>(
            HubMilestonePalletsRangeRule(3600, 86400),
            HubMilestoneRequiredRouteCodeRule("hub milestone needs an entry"),
            HubMilestoneTitleLengthRule(3600),
        ),
    )
}
