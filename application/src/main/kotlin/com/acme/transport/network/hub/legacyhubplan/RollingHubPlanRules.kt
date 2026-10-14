package com.acme.transport.network.hub.legacyhubplan

import com.acme.transport.kit.text.normalize

/** A rule a rolling hub plan must pass. Check returns its problems, or none. */
interface RollingHubPlanRule {
    fun check(item: RollingHubPlan): MutableList<String>
}

data class RollingHubPlanRequiredTitleRule(var message: String) : RollingHubPlanRule {
    override fun check(item: RollingHubPlan): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RollingHubPlanLabelLengthRule(var maxLength: Int) : RollingHubPlanRule {
    override fun check(item: RollingHubPlan): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RollingHubPlanAllowedLabelRule(var allowed: MutableList<String>) : RollingHubPlanRule {
    override fun check(item: RollingHubPlan): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.label))) {
            return mutableListOf<String>("label ${item.label} is too long")
        }
        return mutableListOf<String>()
    }
}

data class RollingHubPlanCodePrefixRule(var prefix: String) : RollingHubPlanRule {
    override fun check(item: RollingHubPlan): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class RollingHubPlanTitleLengthRule(var maxLength: Int) : RollingHubPlanRule {
    override fun check(item: RollingHubPlan): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RollingHubPlanPhoneLengthRule(var maxLength: Int) : RollingHubPlanRule {
    override fun check(item: RollingHubPlan): MutableList<String> {
        val size = item.phone.length
        if (size > maxLength) {
            return mutableListOf<String>("phone is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a rolling hub plan. */
data class RollingHubPlanValidator(var rules: MutableList<RollingHubPlanRule>) {
    fun validate(item: RollingHubPlan): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RollingHubPlan) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RollingHubPlan>): MutableMap<String, MutableList<String>> {
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

fun defaultRollingHubPlanValidator(): RollingHubPlanValidator {
    return RollingHubPlanValidator(
        mutableListOf<RollingHubPlanRule>(
            RollingHubPlanRequiredTitleRule("rolling hub plan needs an entry"),
            RollingHubPlanLabelLengthRule(3600),
            RollingHubPlanAllowedLabelRule(mutableListOf<String>("tiered", "strict", "capped")),
            RollingHubPlanCodePrefixRule("Q"),
            RollingHubPlanTitleLengthRule(32),
            RollingHubPlanPhoneLengthRule(255),
        ),
    )
}
