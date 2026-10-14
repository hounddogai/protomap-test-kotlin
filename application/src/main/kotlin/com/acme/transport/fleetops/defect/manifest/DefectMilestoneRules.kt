package com.acme.transport.fleetops.defect.manifest

/** A rule a defect milestone must pass. Check returns its problems, or none. */
interface DefectMilestoneRule {
    fun check(item: DefectMilestone): MutableList<String>
}

data class DefectMilestoneLimitRangeRule(var min: Int, var max: Int) : DefectMilestoneRule {
    override fun check(item: DefectMilestone): MutableList<String> {
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

data class DefectMilestoneRequiredLabelRule(var message: String) : DefectMilestoneRule {
    override fun check(item: DefectMilestone): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DefectMilestoneCodePrefixRule(var prefix: String) : DefectMilestoneRule {
    override fun check(item: DefectMilestone): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a defect milestone. */
data class DefectMilestoneValidator(var rules: MutableList<DefectMilestoneRule>) {
    fun validate(item: DefectMilestone): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: DefectMilestone) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DefectMilestone>): MutableMap<String, MutableList<String>> {
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

fun defaultDefectMilestoneValidator(): DefectMilestoneValidator {
    return DefectMilestoneValidator(
        mutableListOf<DefectMilestoneRule>(
            DefectMilestoneLimitRangeRule(32, 3600),
            DefectMilestoneRequiredLabelRule("defect milestone needs an entry"),
            DefectMilestoneCodePrefixRule("ACM"),
        ),
    )
}
