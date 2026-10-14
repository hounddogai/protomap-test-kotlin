package com.acme.transport.linehaul.backhaul.rule

/** A rule a batched backhaul target must pass. Check returns its problems, or none. */
interface BatchedBackhaulTargetRule {
    fun check(item: BatchedBackhaulTarget): MutableList<String>
}

data class BatchedBackhaulTargetEndsAtRangeRule(var min: Int, var max: Int) : BatchedBackhaulTargetRule {
    override fun check(item: BatchedBackhaulTarget): MutableList<String> {
        val value = item.endsAt
        if (value < min || value > max) {
            return mutableListOf<String>("ends at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class BatchedBackhaulTargetRequiredSummaryRule(var message: String) : BatchedBackhaulTargetRule {
    override fun check(item: BatchedBackhaulTarget): MutableList<String> {
        if (item.summary.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class BatchedBackhaulTargetSummaryLengthRule(var maxLength: Int) : BatchedBackhaulTargetRule {
    override fun check(item: BatchedBackhaulTarget): MutableList<String> {
        val size = item.summary.length
        if (size > maxLength) {
            return mutableListOf<String>("summary is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a batched backhaul target. */
data class BatchedBackhaulTargetValidator(var rules: MutableList<BatchedBackhaulTargetRule>) {
    fun validate(item: BatchedBackhaulTarget): MutableList<String> {
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

    fun requireValid(item: BatchedBackhaulTarget) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<BatchedBackhaulTarget>): MutableMap<String, MutableList<String>> {
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

fun defaultBatchedBackhaulTargetValidator(): BatchedBackhaulTargetValidator {
    return BatchedBackhaulTargetValidator(
        mutableListOf<BatchedBackhaulTargetRule>(
            BatchedBackhaulTargetEndsAtRangeRule(120, 3600),
            BatchedBackhaulTargetRequiredSummaryRule("batched backhaul target needs an entry"),
            BatchedBackhaulTargetSummaryLengthRule(3600),
        ),
    )
}
