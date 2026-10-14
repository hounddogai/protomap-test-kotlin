package com.acme.transport.linehaul.tender.weightedtenderrollup

/** A rule a pending tender plan must pass. Check returns its problems, or none. */
interface PendingTenderPlanRule {
    fun check(item: PendingTenderPlan): MutableList<String>
}

data class PendingTenderPlanRequiredCodeRule(var message: String) : PendingTenderPlanRule {
    override fun check(item: PendingTenderPlan): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PendingTenderPlanUnitLengthRule(var maxLength: Int) : PendingTenderPlanRule {
    override fun check(item: PendingTenderPlan): MutableList<String> {
        val size = item.unit.length
        if (size > maxLength) {
            return mutableListOf<String>("unit is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PendingTenderPlanPinnedGuardRule(var limit: Double) : PendingTenderPlanRule {
    override fun check(item: PendingTenderPlan): MutableList<String> {
        if (item.pinned && item.rate > limit) {
            return mutableListOf<String>("rate ${item.rate} exceeds $limit while pinned")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a pending tender plan. */
data class PendingTenderPlanValidator(var rules: MutableList<PendingTenderPlanRule>) {
    fun validate(item: PendingTenderPlan): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: PendingTenderPlan) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PendingTenderPlan>): MutableMap<String, MutableList<String>> {
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

fun defaultPendingTenderPlanValidator(): PendingTenderPlanValidator {
    return PendingTenderPlanValidator(
        mutableListOf<PendingTenderPlanRule>(
            PendingTenderPlanRequiredCodeRule("pending tender plan needs a value"),
            PendingTenderPlanUnitLengthRule(1000),
            PendingTenderPlanPinnedGuardRule(7.3),
        ),
    )
}
