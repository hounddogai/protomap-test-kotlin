package com.acme.transport.network.hub.hubdraft

import com.acme.transport.kit.text.normalize

/** A rule a scheduled hub rule must pass. Check returns its problems, or none. */
interface ScheduledHubRuleRule {
    fun check(item: ScheduledHubRule): MutableList<String>
}

data class ScheduledHubRuleNameLengthRule(var maxLength: Int) : ScheduledHubRuleRule {
    override fun check(item: ScheduledHubRule): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledHubRuleRequiredVehicleIdRule(var message: String) : ScheduledHubRuleRule {
    override fun check(item: ScheduledHubRule): MutableList<String> {
        if (item.vehicleId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ScheduledHubRuleAllowedCodeRule(var allowed: MutableList<String>) : ScheduledHubRuleRule {
    override fun check(item: ScheduledHubRule): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is missing")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a scheduled hub rule. */
data class ScheduledHubRuleValidator(var rules: MutableList<ScheduledHubRuleRule>) {
    fun validate(item: ScheduledHubRule): MutableList<String> {
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

    fun requireValid(item: ScheduledHubRule) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ScheduledHubRule>): MutableMap<String, MutableList<String>> {
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

fun defaultScheduledHubRuleValidator(): ScheduledHubRuleValidator {
    return ScheduledHubRuleValidator(
        mutableListOf<ScheduledHubRuleRule>(
            ScheduledHubRuleNameLengthRule(64),
            ScheduledHubRuleRequiredVehicleIdRule("scheduled hub rule needs a value"),
            ScheduledHubRuleAllowedCodeRule(mutableListOf<String>("rolling")),
        ),
    )
}
