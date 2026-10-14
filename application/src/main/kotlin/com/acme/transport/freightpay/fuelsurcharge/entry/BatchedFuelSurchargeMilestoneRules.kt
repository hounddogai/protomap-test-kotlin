package com.acme.transport.freightpay.fuelsurcharge.entry

/** A rule a batched fuel surcharge milestone must pass. Check returns its problems, or none. */
interface BatchedFuelSurchargeMilestoneRule {
    fun check(item: BatchedFuelSurchargeMilestone): MutableList<String>
}

data class BatchedFuelSurchargeMilestoneRequiredCodeRule(
    var message: String,
) : BatchedFuelSurchargeMilestoneRule {
    override fun check(item: BatchedFuelSurchargeMilestone): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class BatchedFuelSurchargeMilestoneRequiredTitleRule(
    var message: String,
) : BatchedFuelSurchargeMilestoneRule {
    override fun check(item: BatchedFuelSurchargeMilestone): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class BatchedFuelSurchargeMilestoneCodePrefixRule(var prefix: String) : BatchedFuelSurchargeMilestoneRule {
    override fun check(item: BatchedFuelSurchargeMilestone): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class BatchedFuelSurchargeMilestoneRequiredTrailerIdRule(
    var message: String,
) : BatchedFuelSurchargeMilestoneRule {
    override fun check(item: BatchedFuelSurchargeMilestone): MutableList<String> {
        if (item.trailerId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a batched fuel surcharge milestone. */
data class BatchedFuelSurchargeMilestoneValidator(var rules: MutableList<BatchedFuelSurchargeMilestoneRule>) {
    fun validate(item: BatchedFuelSurchargeMilestone): MutableList<String> {
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

    fun requireValid(item: BatchedFuelSurchargeMilestone) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<BatchedFuelSurchargeMilestone>): MutableMap<String, MutableList<String>> {
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

fun defaultBatchedFuelSurchargeMilestoneValidator(): BatchedFuelSurchargeMilestoneValidator {
    return BatchedFuelSurchargeMilestoneValidator(
        mutableListOf<BatchedFuelSurchargeMilestoneRule>(
            BatchedFuelSurchargeMilestoneRequiredCodeRule("batched fuel surcharge milestone needs an entry"),
            BatchedFuelSurchargeMilestoneRequiredTitleRule("batched fuel surcharge milestone needs an entry"),
            BatchedFuelSurchargeMilestoneCodePrefixRule("REF"),
            BatchedFuelSurchargeMilestoneRequiredTrailerIdRule(
                "batched fuel surcharge milestone needs an entry",
            ),
        ),
    )
}
