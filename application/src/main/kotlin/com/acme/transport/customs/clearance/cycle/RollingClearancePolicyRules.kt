package com.acme.transport.customs.clearance.cycle

/** A rule a rolling clearance policy must pass. Check returns its problems, or none. */
interface RollingClearancePolicyRule {
    fun check(item: RollingClearancePolicy): MutableList<String>
}

data class RollingClearancePolicyDurationMinutesRangeRule(
    var min: Int,
    var max: Int,
) : RollingClearancePolicyRule {
    override fun check(item: RollingClearancePolicy): MutableList<String> {
        val value = item.durationMinutes
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("duration minutes $value is below $min")
        }
        if (value > max) {
            problems.add("duration minutes $value is above $max")
        }
        return problems
    }
}

data class RollingClearancePolicyRequiredTitleRule(var message: String) : RollingClearancePolicyRule {
    override fun check(item: RollingClearancePolicy): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RollingClearancePolicyRequiredUnitRule(var message: String) : RollingClearancePolicyRule {
    override fun check(item: RollingClearancePolicy): MutableList<String> {
        if (item.unit.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RollingClearancePolicyRequiredSummaryRule(var message: String) : RollingClearancePolicyRule {
    override fun check(item: RollingClearancePolicy): MutableList<String> {
        if (item.summary.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a rolling clearance policy. */
data class RollingClearancePolicyValidator(var rules: MutableList<RollingClearancePolicyRule>) {
    fun validate(item: RollingClearancePolicy): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RollingClearancePolicy) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RollingClearancePolicy>): MutableMap<String, MutableList<String>> {
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

fun defaultRollingClearancePolicyValidator(): RollingClearancePolicyValidator {
    return RollingClearancePolicyValidator(
        mutableListOf<RollingClearancePolicyRule>(
            RollingClearancePolicyDurationMinutesRangeRule(120, 1000),
            RollingClearancePolicyRequiredTitleRule("rolling clearance policy needs this field"),
            RollingClearancePolicyRequiredUnitRule("rolling clearance policy needs a value"),
            RollingClearancePolicyRequiredSummaryRule("rolling clearance policy needs an entry"),
        ),
    )
}
