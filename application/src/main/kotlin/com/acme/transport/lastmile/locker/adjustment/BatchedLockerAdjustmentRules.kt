package com.acme.transport.lastmile.locker.adjustment

/** A rule a batched locker adjustment must pass. Check returns its problems, or none. */
interface BatchedLockerAdjustmentRule {
    fun check(item: BatchedLockerAdjustment): MutableList<String>
}

data class BatchedLockerAdjustmentRequiredCodeRule(var message: String) : BatchedLockerAdjustmentRule {
    override fun check(item: BatchedLockerAdjustment): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class BatchedLockerAdjustmentCodeLengthRule(var maxLength: Int) : BatchedLockerAdjustmentRule {
    override fun check(item: BatchedLockerAdjustment): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class BatchedLockerAdjustmentCategoryLengthRule(var maxLength: Int) : BatchedLockerAdjustmentRule {
    override fun check(item: BatchedLockerAdjustment): MutableList<String> {
        val size = item.category.length
        if (size > maxLength) {
            return mutableListOf<String>("category is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a batched locker adjustment. */
data class BatchedLockerAdjustmentValidator(var rules: MutableList<BatchedLockerAdjustmentRule>) {
    fun validate(item: BatchedLockerAdjustment): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: BatchedLockerAdjustment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<BatchedLockerAdjustment>): MutableMap<String, MutableList<String>> {
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

fun defaultBatchedLockerAdjustmentValidator(): BatchedLockerAdjustmentValidator {
    return BatchedLockerAdjustmentValidator(
        mutableListOf<BatchedLockerAdjustmentRule>(
            BatchedLockerAdjustmentRequiredCodeRule("batched locker adjustment needs a value"),
            BatchedLockerAdjustmentCodeLengthRule(3600),
            BatchedLockerAdjustmentCategoryLengthRule(16),
        ),
    )
}
