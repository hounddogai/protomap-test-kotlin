package com.acme.transport.safety.violation.rollup

/** A rule a batched violation adjustment must pass. Check returns its problems, or none. */
interface BatchedViolationAdjustmentRule {
    fun check(item: BatchedViolationAdjustment): MutableList<String>
}

data class BatchedViolationAdjustmentNameLengthRule(var maxLength: Int) : BatchedViolationAdjustmentRule {
    override fun check(item: BatchedViolationAdjustment): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class BatchedViolationAdjustmentRequiredCodeRule(var message: String) : BatchedViolationAdjustmentRule {
    override fun check(item: BatchedViolationAdjustment): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class BatchedViolationAdjustmentRequiredExternalRefRule(
    var message: String,
) : BatchedViolationAdjustmentRule {
    override fun check(item: BatchedViolationAdjustment): MutableList<String> {
        if (item.externalRef.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class BatchedViolationAdjustmentRequiredTimezoneRule(
    var message: String,
) : BatchedViolationAdjustmentRule {
    override fun check(item: BatchedViolationAdjustment): MutableList<String> {
        if (item.timezone.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class BatchedViolationAdjustmentEtaMinutesRangeRule(
    var min: Int,
    var max: Int,
) : BatchedViolationAdjustmentRule {
    override fun check(item: BatchedViolationAdjustment): MutableList<String> {
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

/** Runs every rule of a batched violation adjustment. */
data class BatchedViolationAdjustmentValidator(var rules: MutableList<BatchedViolationAdjustmentRule>) {
    fun validate(item: BatchedViolationAdjustment): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: BatchedViolationAdjustment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<BatchedViolationAdjustment>): MutableMap<String, MutableList<String>> {
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

fun defaultBatchedViolationAdjustmentValidator(): BatchedViolationAdjustmentValidator {
    return BatchedViolationAdjustmentValidator(
        mutableListOf<BatchedViolationAdjustmentRule>(
            BatchedViolationAdjustmentNameLengthRule(3600),
            BatchedViolationAdjustmentRequiredCodeRule("batched violation adjustment needs a value"),
            BatchedViolationAdjustmentRequiredExternalRefRule("batched violation adjustment needs an entry"),
            BatchedViolationAdjustmentRequiredTimezoneRule("batched violation adjustment needs this field"),
            BatchedViolationAdjustmentEtaMinutesRangeRule(32, 86400),
        ),
    )
}
