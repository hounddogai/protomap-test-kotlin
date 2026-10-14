package com.acme.transport.fleetops.utilization.entry

/** A rule a primary utilization plan must pass. Check returns its problems, or none. */
interface PrimaryUtilizationPlanRule {
    fun check(item: PrimaryUtilizationPlan): MutableList<String>
}

data class PrimaryUtilizationPlanApprovedGuardRule(var limit: Int) : PrimaryUtilizationPlanRule {
    override fun check(item: PrimaryUtilizationPlan): MutableList<String> {
        if (item.approved && item.sequence > limit) {
            return mutableListOf<String>("sequence ${item.sequence} exceeds $limit while approved")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryUtilizationPlanLabelLengthRule(var maxLength: Int) : PrimaryUtilizationPlanRule {
    override fun check(item: PrimaryUtilizationPlan): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryUtilizationPlanCodeLengthRule(var maxLength: Int) : PrimaryUtilizationPlanRule {
    override fun check(item: PrimaryUtilizationPlan): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryUtilizationPlanContactEmailLengthRule(var maxLength: Int) : PrimaryUtilizationPlanRule {
    override fun check(item: PrimaryUtilizationPlan): MutableList<String> {
        val size = item.contactEmail.length
        if (size > maxLength) {
            return mutableListOf<String>("contact email is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryUtilizationPlanSequenceRangeRule(var min: Int, var max: Int) : PrimaryUtilizationPlanRule {
    override fun check(item: PrimaryUtilizationPlan): MutableList<String> {
        val value = item.sequence
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("sequence $value is below $min")
        }
        if (value > max) {
            problems.add("sequence $value is above $max")
        }
        return problems
    }
}

data class PrimaryUtilizationPlanRequiredCodeRule(var message: String) : PrimaryUtilizationPlanRule {
    override fun check(item: PrimaryUtilizationPlan): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a primary utilization plan. */
data class PrimaryUtilizationPlanValidator(var rules: MutableList<PrimaryUtilizationPlanRule>) {
    fun validate(item: PrimaryUtilizationPlan): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: PrimaryUtilizationPlan) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PrimaryUtilizationPlan>): MutableMap<String, MutableList<String>> {
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

fun defaultPrimaryUtilizationPlanValidator(): PrimaryUtilizationPlanValidator {
    return PrimaryUtilizationPlanValidator(
        mutableListOf<PrimaryUtilizationPlanRule>(
            PrimaryUtilizationPlanApprovedGuardRule(64),
            PrimaryUtilizationPlanLabelLengthRule(64),
            PrimaryUtilizationPlanCodeLengthRule(32),
            PrimaryUtilizationPlanContactEmailLengthRule(3600),
            PrimaryUtilizationPlanSequenceRangeRule(16, 255),
            PrimaryUtilizationPlanRequiredCodeRule("primary utilization plan needs an entry"),
        ),
    )
}
