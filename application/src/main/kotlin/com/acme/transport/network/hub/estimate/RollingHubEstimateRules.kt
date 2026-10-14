package com.acme.transport.network.hub.estimate

/** A rule a rolling hub estimate must pass. Check returns its problems, or none. */
interface RollingHubEstimateRule {
    fun check(item: RollingHubEstimate): MutableList<String>
}

data class RollingHubEstimateApprovedGuardRule(var limit: Int) : RollingHubEstimateRule {
    override fun check(item: RollingHubEstimate): MutableList<String> {
        if (item.approved && item.expiresAt > limit) {
            return mutableListOf<String>("expires at ${item.expiresAt} exceeds $limit while approved")
        }
        return mutableListOf<String>()
    }
}

data class RollingHubEstimateRequiredDepotRule(var message: String) : RollingHubEstimateRule {
    override fun check(item: RollingHubEstimate): MutableList<String> {
        if (item.depot.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RollingHubEstimateExpiresAtRangeRule(var min: Int, var max: Int) : RollingHubEstimateRule {
    override fun check(item: RollingHubEstimate): MutableList<String> {
        val value = item.expiresAt
        if (value < min || value > max) {
            return mutableListOf<String>("expires at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a rolling hub estimate. */
data class RollingHubEstimateValidator(var rules: MutableList<RollingHubEstimateRule>) {
    fun validate(item: RollingHubEstimate): MutableList<String> {
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

    fun requireValid(item: RollingHubEstimate) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RollingHubEstimate>): MutableMap<String, MutableList<String>> {
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

fun defaultRollingHubEstimateValidator(): RollingHubEstimateValidator {
    return RollingHubEstimateValidator(
        mutableListOf<RollingHubEstimateRule>(
            RollingHubEstimateApprovedGuardRule(32),
            RollingHubEstimateRequiredDepotRule("rolling hub estimate needs this field"),
            RollingHubEstimateExpiresAtRangeRule(32, 120),
        ),
    )
}
