package com.acme.transport.linehaul.relay.audit

import com.acme.transport.kit.text.normalize

/** A rule a seasonal relay milestone must pass. Check returns its problems, or none. */
interface SeasonalRelayMilestoneRule {
    fun check(item: SeasonalRelayMilestone): MutableList<String>
}

data class SeasonalRelayMilestoneCodeLengthRule(var maxLength: Int) : SeasonalRelayMilestoneRule {
    override fun check(item: SeasonalRelayMilestone): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalRelayMilestoneAllowedCodeRule(
    var allowed: MutableList<String>,
) : SeasonalRelayMilestoneRule {
    override fun check(item: SeasonalRelayMilestone): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is above maximum")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalRelayMilestoneCodePrefixRule(var prefix: String) : SeasonalRelayMilestoneRule {
    override fun check(item: SeasonalRelayMilestone): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a seasonal relay milestone. */
data class SeasonalRelayMilestoneValidator(var rules: MutableList<SeasonalRelayMilestoneRule>) {
    fun validate(item: SeasonalRelayMilestone): MutableList<String> {
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

    fun requireValid(item: SeasonalRelayMilestone) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<SeasonalRelayMilestone>): MutableMap<String, MutableList<String>> {
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

fun defaultSeasonalRelayMilestoneValidator(): SeasonalRelayMilestoneValidator {
    return SeasonalRelayMilestoneValidator(
        mutableListOf<SeasonalRelayMilestoneRule>(
            SeasonalRelayMilestoneCodeLengthRule(86400),
            SeasonalRelayMilestoneAllowedCodeRule(mutableListOf<String>("lenient", "global")),
            SeasonalRelayMilestoneCodePrefixRule("ACM"),
        ),
    )
}
