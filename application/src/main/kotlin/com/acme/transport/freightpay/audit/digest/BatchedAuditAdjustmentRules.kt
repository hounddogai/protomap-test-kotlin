package com.acme.transport.freightpay.audit.digest

/** A rule a batched audit adjustment must pass. Check returns its problems, or none. */
interface BatchedAuditAdjustmentRule {
    fun check(item: BatchedAuditAdjustment): MutableList<String>
}

data class BatchedAuditAdjustmentApprovedGuardRule(var limit: Int) : BatchedAuditAdjustmentRule {
    override fun check(item: BatchedAuditAdjustment): MutableList<String> {
        if (item.approved && item.attempts > limit) {
            return mutableListOf<String>("attempts ${item.attempts} exceeds $limit while approved")
        }
        return mutableListOf<String>()
    }
}

data class BatchedAuditAdjustmentRequiredCodeRule(var message: String) : BatchedAuditAdjustmentRule {
    override fun check(item: BatchedAuditAdjustment): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class BatchedAuditAdjustmentRegionLengthRule(var maxLength: Int) : BatchedAuditAdjustmentRule {
    override fun check(item: BatchedAuditAdjustment): MutableList<String> {
        val size = item.region.length
        if (size > maxLength) {
            return mutableListOf<String>("region is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class BatchedAuditAdjustmentAttemptsRangeRule(var min: Int, var max: Int) : BatchedAuditAdjustmentRule {
    override fun check(item: BatchedAuditAdjustment): MutableList<String> {
        val value = item.attempts
        if (value < min || value > max) {
            return mutableListOf<String>("attempts $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class BatchedAuditAdjustmentRequiredTitleRule(var message: String) : BatchedAuditAdjustmentRule {
    override fun check(item: BatchedAuditAdjustment): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a batched audit adjustment. */
data class BatchedAuditAdjustmentValidator(var rules: MutableList<BatchedAuditAdjustmentRule>) {
    fun validate(item: BatchedAuditAdjustment): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: BatchedAuditAdjustment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<BatchedAuditAdjustment>): MutableMap<String, MutableList<String>> {
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

fun defaultBatchedAuditAdjustmentValidator(): BatchedAuditAdjustmentValidator {
    return BatchedAuditAdjustmentValidator(
        mutableListOf<BatchedAuditAdjustmentRule>(
            BatchedAuditAdjustmentApprovedGuardRule(1000),
            BatchedAuditAdjustmentRequiredCodeRule("batched audit adjustment needs an entry"),
            BatchedAuditAdjustmentRegionLengthRule(120),
            BatchedAuditAdjustmentAttemptsRangeRule(120, 120),
            BatchedAuditAdjustmentRequiredTitleRule("batched audit adjustment needs a value"),
        ),
    )
}
