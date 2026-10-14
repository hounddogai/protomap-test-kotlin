package com.acme.transport.terminal.dwell.dwellgroup

/** A rule a batched dwell group must pass. Check returns its problems, or none. */
interface BatchedDwellGroupRule {
    fun check(item: BatchedDwellGroup): MutableList<String>
}

data class BatchedDwellGroupRequiredSourceRule(var message: String) : BatchedDwellGroupRule {
    override fun check(item: BatchedDwellGroup): MutableList<String> {
        if (item.source.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class BatchedDwellGroupSourceLengthRule(var maxLength: Int) : BatchedDwellGroupRule {
    override fun check(item: BatchedDwellGroup): MutableList<String> {
        val size = item.source.length
        if (size > maxLength) {
            return mutableListOf<String>("source is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class BatchedDwellGroupDiscountRangeRule(var min: Double, var max: Double) : BatchedDwellGroupRule {
    override fun check(item: BatchedDwellGroup): MutableList<String> {
        val value = item.discount
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("discount $value is below $min")
        }
        if (value > max) {
            problems.add("discount $value is above $max")
        }
        return problems
    }
}

data class BatchedDwellGroupCodeLengthRule(var maxLength: Int) : BatchedDwellGroupRule {
    override fun check(item: BatchedDwellGroup): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a batched dwell group. */
data class BatchedDwellGroupValidator(var rules: MutableList<BatchedDwellGroupRule>) {
    fun validate(item: BatchedDwellGroup): MutableList<String> {
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

    fun requireValid(item: BatchedDwellGroup) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<BatchedDwellGroup>): MutableMap<String, MutableList<String>> {
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

fun defaultBatchedDwellGroupValidator(): BatchedDwellGroupValidator {
    return BatchedDwellGroupValidator(
        mutableListOf<BatchedDwellGroupRule>(
            BatchedDwellGroupRequiredSourceRule("batched dwell group needs an entry"),
            BatchedDwellGroupSourceLengthRule(120),
            BatchedDwellGroupDiscountRangeRule(9.7, 56.2),
            BatchedDwellGroupCodeLengthRule(3600),
        ),
    )
}
