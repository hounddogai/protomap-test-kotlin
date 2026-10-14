package com.acme.transport.freightpay.audit.plan

/** A rule a batched audit milestone must pass. Check returns its problems, or none. */
interface BatchedAuditMilestoneRule {
    fun check(item: BatchedAuditMilestone): MutableList<String>
}

data class BatchedAuditMilestoneCodeLengthRule(var maxLength: Int) : BatchedAuditMilestoneRule {
    override fun check(item: BatchedAuditMilestone): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class BatchedAuditMilestoneRequiredContactEmailRule(var message: String) : BatchedAuditMilestoneRule {
    override fun check(item: BatchedAuditMilestone): MutableList<String> {
        if (item.contactEmail.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class BatchedAuditMilestoneCodePrefixRule(var prefix: String) : BatchedAuditMilestoneRule {
    override fun check(item: BatchedAuditMilestone): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class BatchedAuditMilestoneRequiredCodeRule(var message: String) : BatchedAuditMilestoneRule {
    override fun check(item: BatchedAuditMilestone): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class BatchedAuditMilestoneCeilingRangeRule(var min: Double, var max: Double) : BatchedAuditMilestoneRule {
    override fun check(item: BatchedAuditMilestone): MutableList<String> {
        val value = item.ceiling
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("ceiling $value is below $min")
        }
        if (value > max) {
            problems.add("ceiling $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a batched audit milestone. */
data class BatchedAuditMilestoneValidator(var rules: MutableList<BatchedAuditMilestoneRule>) {
    fun validate(item: BatchedAuditMilestone): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: BatchedAuditMilestone) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<BatchedAuditMilestone>): MutableMap<String, MutableList<String>> {
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

fun defaultBatchedAuditMilestoneValidator(): BatchedAuditMilestoneValidator {
    return BatchedAuditMilestoneValidator(
        mutableListOf<BatchedAuditMilestoneRule>(
            BatchedAuditMilestoneCodeLengthRule(3600),
            BatchedAuditMilestoneRequiredContactEmailRule("batched audit milestone needs this field"),
            BatchedAuditMilestoneCodePrefixRule("Q"),
            BatchedAuditMilestoneRequiredCodeRule("batched audit milestone needs an entry"),
            BatchedAuditMilestoneCeilingRangeRule(8.5, 1509.4),
        ),
    )
}
