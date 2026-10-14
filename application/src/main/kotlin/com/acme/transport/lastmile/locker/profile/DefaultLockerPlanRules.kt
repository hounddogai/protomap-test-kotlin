package com.acme.transport.lastmile.locker.profile

/** A rule a default locker plan must pass. Check returns its problems, or none. */
interface DefaultLockerPlanRule {
    fun check(item: DefaultLockerPlan): MutableList<String>
}

data class DefaultLockerPlanRequiredSummaryRule(var message: String) : DefaultLockerPlanRule {
    override fun check(item: DefaultLockerPlan): MutableList<String> {
        if (item.summary.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DefaultLockerPlanCodeLengthRule(var maxLength: Int) : DefaultLockerPlanRule {
    override fun check(item: DefaultLockerPlan): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DefaultLockerPlanMaximumRangeRule(var min: Double, var max: Double) : DefaultLockerPlanRule {
    override fun check(item: DefaultLockerPlan): MutableList<String> {
        val value = item.maximum
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("maximum $value is below $min")
        }
        if (value > max) {
            problems.add("maximum $value is above $max")
        }
        return problems
    }
}

data class DefaultLockerPlanWeightRangeRule(var min: Double, var max: Double) : DefaultLockerPlanRule {
    override fun check(item: DefaultLockerPlan): MutableList<String> {
        val value = item.weight
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("weight $value is below $min")
        }
        if (value > max) {
            problems.add("weight $value is above $max")
        }
        return problems
    }
}

data class DefaultLockerPlanOwnerIdLengthRule(var maxLength: Int) : DefaultLockerPlanRule {
    override fun check(item: DefaultLockerPlan): MutableList<String> {
        val size = item.ownerId.length
        if (size > maxLength) {
            return mutableListOf<String>("owner id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DefaultLockerPlanExpiresAtRangeRule(var min: Int, var max: Int) : DefaultLockerPlanRule {
    override fun check(item: DefaultLockerPlan): MutableList<String> {
        val value = item.expiresAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("expires at $value is below $min")
        }
        if (value > max) {
            problems.add("expires at $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a default locker plan. */
data class DefaultLockerPlanValidator(var rules: MutableList<DefaultLockerPlanRule>) {
    fun validate(item: DefaultLockerPlan): MutableList<String> {
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

    fun requireValid(item: DefaultLockerPlan) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DefaultLockerPlan>): MutableMap<String, MutableList<String>> {
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

fun defaultDefaultLockerPlanValidator(): DefaultLockerPlanValidator {
    return DefaultLockerPlanValidator(
        mutableListOf<DefaultLockerPlanRule>(
            DefaultLockerPlanRequiredSummaryRule("default locker plan needs a value"),
            DefaultLockerPlanCodeLengthRule(3600),
            DefaultLockerPlanMaximumRangeRule(5.2, 3428.9),
            DefaultLockerPlanWeightRangeRule(8.6, 2629.6),
            DefaultLockerPlanOwnerIdLengthRule(64),
            DefaultLockerPlanExpiresAtRangeRule(80, 120),
        ),
    )
}
