package com.acme.transport.crewing.restbreak.breaksummary

/** A rule a batched rest break summary must pass. Check returns its problems, or none. */
interface BatchedRestBreakSummaryRule {
    fun check(item: BatchedRestBreakSummary): MutableList<String>
}

data class BatchedRestBreakSummaryRequiredContactEmailRule(var message: String) : BatchedRestBreakSummaryRule {
    override fun check(item: BatchedRestBreakSummary): MutableList<String> {
        if (item.contactEmail.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class BatchedRestBreakSummaryDiscountRangeRule(
    var min: Double,
    var max: Double,
) : BatchedRestBreakSummaryRule {
    override fun check(item: BatchedRestBreakSummary): MutableList<String> {
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

data class BatchedRestBreakSummaryCodeLengthRule(var maxLength: Int) : BatchedRestBreakSummaryRule {
    override fun check(item: BatchedRestBreakSummary): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class BatchedRestBreakSummaryNameLengthRule(var maxLength: Int) : BatchedRestBreakSummaryRule {
    override fun check(item: BatchedRestBreakSummary): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a batched rest break summary. */
data class BatchedRestBreakSummaryValidator(var rules: MutableList<BatchedRestBreakSummaryRule>) {
    fun validate(item: BatchedRestBreakSummary): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: BatchedRestBreakSummary) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<BatchedRestBreakSummary>): MutableMap<String, MutableList<String>> {
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

fun defaultBatchedRestBreakSummaryValidator(): BatchedRestBreakSummaryValidator {
    return BatchedRestBreakSummaryValidator(
        mutableListOf<BatchedRestBreakSummaryRule>(
            BatchedRestBreakSummaryRequiredContactEmailRule("batched rest break summary needs this field"),
            BatchedRestBreakSummaryDiscountRangeRule(9.6, 2640.0),
            BatchedRestBreakSummaryCodeLengthRule(255),
            BatchedRestBreakSummaryNameLengthRule(86400),
        ),
    )
}
