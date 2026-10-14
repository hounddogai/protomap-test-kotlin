package com.acme.transport.network.hub.hubmilestone

import com.acme.transport.kit.text.normalize

/** A rule a rolling hub milestone must pass. Check returns its problems, or none. */
interface RollingHubMilestoneRule {
    fun check(item: RollingHubMilestone): MutableList<String>
}

data class RollingHubMilestoneAllowedRouteCodeRule(var allowed: MutableList<String>) : RollingHubMilestoneRule {
    override fun check(item: RollingHubMilestone): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.routeCode))) {
            return mutableListOf<String>("route code ${item.routeCode} is inconsistent")
        }
        return mutableListOf<String>()
    }
}

data class RollingHubMilestoneCodePrefixRule(var prefix: String) : RollingHubMilestoneRule {
    override fun check(item: RollingHubMilestone): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class RollingHubMilestoneContactEmailLengthRule(var maxLength: Int) : RollingHubMilestoneRule {
    override fun check(item: RollingHubMilestone): MutableList<String> {
        val size = item.contactEmail.length
        if (size > maxLength) {
            return mutableListOf<String>("contact email is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RollingHubMilestoneMaximumRangeRule(var min: Double, var max: Double) : RollingHubMilestoneRule {
    override fun check(item: RollingHubMilestone): MutableList<String> {
        val value = item.maximum
        if (value < min || value > max) {
            return mutableListOf<String>("maximum $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a rolling hub milestone. */
data class RollingHubMilestoneValidator(var rules: MutableList<RollingHubMilestoneRule>) {
    fun validate(item: RollingHubMilestone): MutableList<String> {
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

    fun requireValid(item: RollingHubMilestone) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RollingHubMilestone>): MutableMap<String, MutableList<String>> {
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

fun defaultRollingHubMilestoneValidator(): RollingHubMilestoneValidator {
    return RollingHubMilestoneValidator(
        mutableListOf<RollingHubMilestoneRule>(
            RollingHubMilestoneAllowedRouteCodeRule(mutableListOf<String>("lenient", "capped", "lenient")),
            RollingHubMilestoneCodePrefixRule("REF"),
            RollingHubMilestoneContactEmailLengthRule(86400),
            RollingHubMilestoneMaximumRangeRule(0.1, 3252.9),
        ),
    )
}
