package com.acme.transport.freightpay.dispute.disputeassignment

/** A rule a batched dispute rule must pass. Check returns its problems, or none. */
interface BatchedDisputeRuleRule {
    fun check(item: BatchedDisputeRule): MutableList<String>
}

data class BatchedDisputeRuleRequiredPhoneRule(var message: String) : BatchedDisputeRuleRule {
    override fun check(item: BatchedDisputeRule): MutableList<String> {
        if (item.phone.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class BatchedDisputeRuleRequiredCodeRule(var message: String) : BatchedDisputeRuleRule {
    override fun check(item: BatchedDisputeRule): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class BatchedDisputeRuleRequiredSummaryRule(var message: String) : BatchedDisputeRuleRule {
    override fun check(item: BatchedDisputeRule): MutableList<String> {
        if (item.summary.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a batched dispute rule. */
data class BatchedDisputeRuleValidator(var rules: MutableList<BatchedDisputeRuleRule>) {
    fun validate(item: BatchedDisputeRule): MutableList<String> {
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

    fun requireValid(item: BatchedDisputeRule) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<BatchedDisputeRule>): MutableMap<String, MutableList<String>> {
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

fun defaultBatchedDisputeRuleValidator(): BatchedDisputeRuleValidator {
    return BatchedDisputeRuleValidator(
        mutableListOf<BatchedDisputeRuleRule>(
            BatchedDisputeRuleRequiredPhoneRule("batched dispute rule needs an entry"),
            BatchedDisputeRuleRequiredCodeRule("batched dispute rule needs a value"),
            BatchedDisputeRuleRequiredSummaryRule("batched dispute rule needs an entry"),
        ),
    )
}
