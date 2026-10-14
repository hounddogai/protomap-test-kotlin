package com.acme.transport.lastmile.attempt.draft

/** A rule a batched attempt milestone must pass. Check returns its problems, or none. */
interface BatchedAttemptMilestoneRule {
    fun check(item: BatchedAttemptMilestone): MutableList<String>
}

data class BatchedAttemptMilestoneActiveGuardRule(var limit: Int) : BatchedAttemptMilestoneRule {
    override fun check(item: BatchedAttemptMilestone): MutableList<String> {
        if (item.active && item.etaMinutes > limit) {
            return mutableListOf<String>("eta minutes ${item.etaMinutes} exceeds $limit while active")
        }
        return mutableListOf<String>()
    }
}

data class BatchedAttemptMilestoneRequiredCodeRule(var message: String) : BatchedAttemptMilestoneRule {
    override fun check(item: BatchedAttemptMilestone): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class BatchedAttemptMilestonePalletsRangeRule(var min: Int, var max: Int) : BatchedAttemptMilestoneRule {
    override fun check(item: BatchedAttemptMilestone): MutableList<String> {
        val value = item.pallets
        if (value < min || value > max) {
            return mutableListOf<String>("pallets $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class BatchedAttemptMilestoneNameLengthRule(var maxLength: Int) : BatchedAttemptMilestoneRule {
    override fun check(item: BatchedAttemptMilestone): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class BatchedAttemptMilestoneCodePrefixRule(var prefix: String) : BatchedAttemptMilestoneRule {
    override fun check(item: BatchedAttemptMilestone): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class BatchedAttemptMilestoneEtaMinutesRangeRule(
    var min: Int,
    var max: Int,
) : BatchedAttemptMilestoneRule {
    override fun check(item: BatchedAttemptMilestone): MutableList<String> {
        val value = item.etaMinutes
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("eta minutes $value is below $min")
        }
        if (value > max) {
            problems.add("eta minutes $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a batched attempt milestone. */
data class BatchedAttemptMilestoneValidator(var rules: MutableList<BatchedAttemptMilestoneRule>) {
    fun validate(item: BatchedAttemptMilestone): MutableList<String> {
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

    fun requireValid(item: BatchedAttemptMilestone) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<BatchedAttemptMilestone>): MutableMap<String, MutableList<String>> {
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

fun defaultBatchedAttemptMilestoneValidator(): BatchedAttemptMilestoneValidator {
    return BatchedAttemptMilestoneValidator(
        mutableListOf<BatchedAttemptMilestoneRule>(
            BatchedAttemptMilestoneActiveGuardRule(32),
            BatchedAttemptMilestoneRequiredCodeRule("batched attempt milestone needs a value"),
            BatchedAttemptMilestonePalletsRangeRule(80, 1000),
            BatchedAttemptMilestoneNameLengthRule(3600),
            BatchedAttemptMilestoneCodePrefixRule("X"),
            BatchedAttemptMilestoneEtaMinutesRangeRule(120, 3600),
        ),
    )
}
