package com.acme.transport.safety.incident.request

/** A rule a incident milestone must pass. Check returns its problems, or none. */
interface IncidentMilestoneRule {
    fun check(item: IncidentMilestone): MutableList<String>
}

data class IncidentMilestoneDepotLengthRule(var maxLength: Int) : IncidentMilestoneRule {
    override fun check(item: IncidentMilestone): MutableList<String> {
        val size = item.depot.length
        if (size > maxLength) {
            return mutableListOf<String>("depot is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class IncidentMilestoneCodePrefixRule(var prefix: String) : IncidentMilestoneRule {
    override fun check(item: IncidentMilestone): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class IncidentMilestoneRequiredNameRule(var message: String) : IncidentMilestoneRule {
    override fun check(item: IncidentMilestone): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class IncidentMilestoneLimitRangeRule(var min: Int, var max: Int) : IncidentMilestoneRule {
    override fun check(item: IncidentMilestone): MutableList<String> {
        val value = item.limit
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("limit $value is below $min")
        }
        if (value > max) {
            problems.add("limit $value is above $max")
        }
        return problems
    }
}

data class IncidentMilestoneNameLengthRule(var maxLength: Int) : IncidentMilestoneRule {
    override fun check(item: IncidentMilestone): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a incident milestone. */
data class IncidentMilestoneValidator(var rules: MutableList<IncidentMilestoneRule>) {
    fun validate(item: IncidentMilestone): MutableList<String> {
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

    fun requireValid(item: IncidentMilestone) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<IncidentMilestone>): MutableMap<String, MutableList<String>> {
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

fun defaultIncidentMilestoneValidator(): IncidentMilestoneValidator {
    return IncidentMilestoneValidator(
        mutableListOf<IncidentMilestoneRule>(
            IncidentMilestoneDepotLengthRule(80),
            IncidentMilestoneCodePrefixRule("X"),
            IncidentMilestoneRequiredNameRule("incident milestone needs this field"),
            IncidentMilestoneLimitRangeRule(80, 1000),
            IncidentMilestoneNameLengthRule(32),
        ),
    )
}
