package com.acme.transport.crewing.roster.agreement

/** A rule a batched roster adjustment must pass. Check returns its problems, or none. */
interface BatchedRosterAdjustmentRule {
    fun check(item: BatchedRosterAdjustment): MutableList<String>
}

data class BatchedRosterAdjustmentRequiredShiftCodeRule(var message: String) : BatchedRosterAdjustmentRule {
    override fun check(item: BatchedRosterAdjustment): MutableList<String> {
        if (item.shiftCode.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class BatchedRosterAdjustmentLabelLengthRule(var maxLength: Int) : BatchedRosterAdjustmentRule {
    override fun check(item: BatchedRosterAdjustment): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class BatchedRosterAdjustmentRequiredReviewerRule(var message: String) : BatchedRosterAdjustmentRule {
    override fun check(item: BatchedRosterAdjustment): MutableList<String> {
        if (item.reviewer.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class BatchedRosterAdjustmentPriorityRangeRule(var min: Int, var max: Int) : BatchedRosterAdjustmentRule {
    override fun check(item: BatchedRosterAdjustment): MutableList<String> {
        val value = item.priority
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("priority $value is below $min")
        }
        if (value > max) {
            problems.add("priority $value is above $max")
        }
        return problems
    }
}

data class BatchedRosterAdjustmentCodePrefixRule(var prefix: String) : BatchedRosterAdjustmentRule {
    override fun check(item: BatchedRosterAdjustment): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class BatchedRosterAdjustmentCodeLengthRule(var maxLength: Int) : BatchedRosterAdjustmentRule {
    override fun check(item: BatchedRosterAdjustment): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a batched roster adjustment. */
data class BatchedRosterAdjustmentValidator(var rules: MutableList<BatchedRosterAdjustmentRule>) {
    fun validate(item: BatchedRosterAdjustment): MutableList<String> {
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

    fun requireValid(item: BatchedRosterAdjustment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<BatchedRosterAdjustment>): MutableMap<String, MutableList<String>> {
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

fun defaultBatchedRosterAdjustmentValidator(): BatchedRosterAdjustmentValidator {
    return BatchedRosterAdjustmentValidator(
        mutableListOf<BatchedRosterAdjustmentRule>(
            BatchedRosterAdjustmentRequiredShiftCodeRule("batched roster adjustment needs an entry"),
            BatchedRosterAdjustmentLabelLengthRule(86400),
            BatchedRosterAdjustmentRequiredReviewerRule("batched roster adjustment needs a value"),
            BatchedRosterAdjustmentPriorityRangeRule(16, 64),
            BatchedRosterAdjustmentCodePrefixRule("OPS"),
            BatchedRosterAdjustmentCodeLengthRule(3600),
        ),
    )
}
