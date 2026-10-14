package com.acme.transport.safety.nearmiss.plan

/** A rule a batched near miss plan must pass. Check returns its problems, or none. */
interface BatchedNearMissPlanRule {
    fun check(item: BatchedNearMissPlan): MutableList<String>
}

data class BatchedNearMissPlanVisibleGuardRule(var limit: Double) : BatchedNearMissPlanRule {
    override fun check(item: BatchedNearMissPlan): MutableList<String> {
        if (item.visible && item.amount > limit) {
            return mutableListOf<String>("amount ${item.amount} exceeds $limit while visible")
        }
        return mutableListOf<String>()
    }
}

data class BatchedNearMissPlanRequiredNameRule(var message: String) : BatchedNearMissPlanRule {
    override fun check(item: BatchedNearMissPlan): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class BatchedNearMissPlanNameLengthRule(var maxLength: Int) : BatchedNearMissPlanRule {
    override fun check(item: BatchedNearMissPlan): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class BatchedNearMissPlanRequiredVehicleIdRule(var message: String) : BatchedNearMissPlanRule {
    override fun check(item: BatchedNearMissPlan): MutableList<String> {
        if (item.vehicleId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class BatchedNearMissPlanAmountRangeRule(var min: Double, var max: Double) : BatchedNearMissPlanRule {
    override fun check(item: BatchedNearMissPlan): MutableList<String> {
        val value = item.amount
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("amount $value is below $min")
        }
        if (value > max) {
            problems.add("amount $value is above $max")
        }
        return problems
    }
}

data class BatchedNearMissPlanCodeLengthRule(var maxLength: Int) : BatchedNearMissPlanRule {
    override fun check(item: BatchedNearMissPlan): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a batched near miss plan. */
data class BatchedNearMissPlanValidator(var rules: MutableList<BatchedNearMissPlanRule>) {
    fun validate(item: BatchedNearMissPlan): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: BatchedNearMissPlan) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<BatchedNearMissPlan>): MutableMap<String, MutableList<String>> {
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

fun defaultBatchedNearMissPlanValidator(): BatchedNearMissPlanValidator {
    return BatchedNearMissPlanValidator(
        mutableListOf<BatchedNearMissPlanRule>(
            BatchedNearMissPlanVisibleGuardRule(3.2),
            BatchedNearMissPlanRequiredNameRule("batched near miss plan needs a value"),
            BatchedNearMissPlanNameLengthRule(32),
            BatchedNearMissPlanRequiredVehicleIdRule("batched near miss plan needs an entry"),
            BatchedNearMissPlanAmountRangeRule(3.2, 3523.6),
            BatchedNearMissPlanCodeLengthRule(16),
        ),
    )
}
