package com.acme.transport.network.servicearea.areabatch

/** A rule a batched service area entry must pass. Check returns its problems, or none. */
interface BatchedServiceAreaEntryRule {
    fun check(item: BatchedServiceAreaEntry): MutableList<String>
}

data class BatchedServiceAreaEntryCurrencyLengthRule(var maxLength: Int) : BatchedServiceAreaEntryRule {
    override fun check(item: BatchedServiceAreaEntry): MutableList<String> {
        val size = item.currency.length
        if (size > maxLength) {
            return mutableListOf<String>("currency is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class BatchedServiceAreaEntryLimitRangeRule(var min: Int, var max: Int) : BatchedServiceAreaEntryRule {
    override fun check(item: BatchedServiceAreaEntry): MutableList<String> {
        val value = item.limit
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("limit $value is below $min")
        }
        if (value > max) {
            problems.add("limit $value is above $max")
        }
        return problems
    }
}

data class BatchedServiceAreaEntrySegmentLengthRule(var maxLength: Int) : BatchedServiceAreaEntryRule {
    override fun check(item: BatchedServiceAreaEntry): MutableList<String> {
        val size = item.segment.length
        if (size > maxLength) {
            return mutableListOf<String>("segment is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a batched service area entry. */
data class BatchedServiceAreaEntryValidator(var rules: MutableList<BatchedServiceAreaEntryRule>) {
    fun validate(item: BatchedServiceAreaEntry): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: BatchedServiceAreaEntry) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<BatchedServiceAreaEntry>): MutableMap<String, MutableList<String>> {
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

fun defaultBatchedServiceAreaEntryValidator(): BatchedServiceAreaEntryValidator {
    return BatchedServiceAreaEntryValidator(
        mutableListOf<BatchedServiceAreaEntryRule>(
            BatchedServiceAreaEntryCurrencyLengthRule(16),
            BatchedServiceAreaEntryLimitRangeRule(64, 3600),
            BatchedServiceAreaEntrySegmentLengthRule(1000),
        ),
    )
}
