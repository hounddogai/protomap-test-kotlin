package com.acme.transport.safety.incident.defaultincidentplan

import com.acme.transport.kit.text.normalize

/** A rule a seasonal incident milestone must pass. Check returns its problems, or none. */
interface SeasonalIncidentMilestoneRule {
    fun check(item: SeasonalIncidentMilestone): MutableList<String>
}

data class SeasonalIncidentMilestoneAllowedCodeRule(
    var allowed: MutableList<String>,
) : SeasonalIncidentMilestoneRule {
    override fun check(item: SeasonalIncidentMilestone): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is stale")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalIncidentMilestoneCreatedAtRangeRule(
    var min: Int,
    var max: Int,
) : SeasonalIncidentMilestoneRule {
    override fun check(item: SeasonalIncidentMilestone): MutableList<String> {
        val value = item.createdAt
        if (value < min || value > max) {
            return mutableListOf<String>("created at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalIncidentMilestoneCodePrefixRule(var prefix: String) : SeasonalIncidentMilestoneRule {
    override fun check(item: SeasonalIncidentMilestone): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a seasonal incident milestone. */
data class SeasonalIncidentMilestoneValidator(var rules: MutableList<SeasonalIncidentMilestoneRule>) {
    fun validate(item: SeasonalIncidentMilestone): MutableList<String> {
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

    fun requireValid(item: SeasonalIncidentMilestone) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<SeasonalIncidentMilestone>): MutableMap<String, MutableList<String>> {
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

fun defaultSeasonalIncidentMilestoneValidator(): SeasonalIncidentMilestoneValidator {
    return SeasonalIncidentMilestoneValidator(
        mutableListOf<SeasonalIncidentMilestoneRule>(
            SeasonalIncidentMilestoneAllowedCodeRule(mutableListOf<String>("seasonal", "capped", "adaptive")),
            SeasonalIncidentMilestoneCreatedAtRangeRule(64, 3600),
            SeasonalIncidentMilestoneCodePrefixRule("REF"),
        ),
    )
}
