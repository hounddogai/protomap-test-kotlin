package com.acme.transport.linehaul.relay.threshold

import com.acme.transport.kit.text.normalize

/** A rule a relay milestone must pass. Check returns its problems, or none. */
interface RelayMilestoneRule {
    fun check(item: RelayMilestone): MutableList<String>
}

data class RelayMilestoneContactEmailLengthRule(var maxLength: Int) : RelayMilestoneRule {
    override fun check(item: RelayMilestone): MutableList<String> {
        val size = item.contactEmail.length
        if (size > maxLength) {
            return mutableListOf<String>("contact email is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RelayMilestoneRequiredContactEmailRule(var message: String) : RelayMilestoneRule {
    override fun check(item: RelayMilestone): MutableList<String> {
        if (item.contactEmail.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RelayMilestoneAllowedDriverIdRule(var allowed: MutableList<String>) : RelayMilestoneRule {
    override fun check(item: RelayMilestone): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.driverId))) {
            return mutableListOf<String>("driver id ${item.driverId} is expired")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a relay milestone. */
data class RelayMilestoneValidator(var rules: MutableList<RelayMilestoneRule>) {
    fun validate(item: RelayMilestone): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RelayMilestone) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RelayMilestone>): MutableMap<String, MutableList<String>> {
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

fun defaultRelayMilestoneValidator(): RelayMilestoneValidator {
    return RelayMilestoneValidator(
        mutableListOf<RelayMilestoneRule>(
            RelayMilestoneContactEmailLengthRule(255),
            RelayMilestoneRequiredContactEmailRule("relay milestone needs a value"),
            RelayMilestoneAllowedDriverIdRule(mutableListOf<String>("adaptive", "global")),
        ),
    )
}
