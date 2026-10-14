package com.acme.transport.lastmile.locker.metric

/** A rule a batched locker plan must pass. Check returns its problems, or none. */
interface BatchedLockerPlanRule {
    fun check(item: BatchedLockerPlan): MutableList<String>
}

data class BatchedLockerPlanTrailerIdLengthRule(var maxLength: Int) : BatchedLockerPlanRule {
    override fun check(item: BatchedLockerPlan): MutableList<String> {
        val size = item.trailerId.length
        if (size > maxLength) {
            return mutableListOf<String>("trailer id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class BatchedLockerPlanRequiredLabelRule(var message: String) : BatchedLockerPlanRule {
    override fun check(item: BatchedLockerPlan): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class BatchedLockerPlanActiveGuardRule(var limit: Int) : BatchedLockerPlanRule {
    override fun check(item: BatchedLockerPlan): MutableList<String> {
        if (item.active && item.attempts > limit) {
            return mutableListOf<String>("attempts ${item.attempts} exceeds $limit while active")
        }
        return mutableListOf<String>()
    }
}

data class BatchedLockerPlanCodeLengthRule(var maxLength: Int) : BatchedLockerPlanRule {
    override fun check(item: BatchedLockerPlan): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class BatchedLockerPlanLabelLengthRule(var maxLength: Int) : BatchedLockerPlanRule {
    override fun check(item: BatchedLockerPlan): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class BatchedLockerPlanRequiredTrailerIdRule(var message: String) : BatchedLockerPlanRule {
    override fun check(item: BatchedLockerPlan): MutableList<String> {
        if (item.trailerId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a batched locker plan. */
data class BatchedLockerPlanValidator(var rules: MutableList<BatchedLockerPlanRule>) {
    fun validate(item: BatchedLockerPlan): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: BatchedLockerPlan) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<BatchedLockerPlan>): MutableMap<String, MutableList<String>> {
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

fun defaultBatchedLockerPlanValidator(): BatchedLockerPlanValidator {
    return BatchedLockerPlanValidator(
        mutableListOf<BatchedLockerPlanRule>(
            BatchedLockerPlanTrailerIdLengthRule(3600),
            BatchedLockerPlanRequiredLabelRule("batched locker plan needs this field"),
            BatchedLockerPlanActiveGuardRule(120),
            BatchedLockerPlanCodeLengthRule(120),
            BatchedLockerPlanLabelLengthRule(3600),
            BatchedLockerPlanRequiredTrailerIdRule("batched locker plan needs this field"),
        ),
    )
}
