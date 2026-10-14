package com.acme.transport.terminal.traileryard.recorditem

/** A rule a batched trailer yard draft must pass. Check returns its problems, or none. */
interface BatchedTrailerYardDraftRule {
    fun check(item: BatchedTrailerYardDraft): MutableList<String>
}

data class BatchedTrailerYardDraftCostRangeRule(
    var min: Double,
    var max: Double,
) : BatchedTrailerYardDraftRule {
    override fun check(item: BatchedTrailerYardDraft): MutableList<String> {
        val value = item.cost
        if (value < min || value > max) {
            return mutableListOf<String>("cost $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class BatchedTrailerYardDraftCodeLengthRule(var maxLength: Int) : BatchedTrailerYardDraftRule {
    override fun check(item: BatchedTrailerYardDraft): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class BatchedTrailerYardDraftCodePrefixRule(var prefix: String) : BatchedTrailerYardDraftRule {
    override fun check(item: BatchedTrailerYardDraft): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class BatchedTrailerYardDraftEndsAtRangeRule(var min: Int, var max: Int) : BatchedTrailerYardDraftRule {
    override fun check(item: BatchedTrailerYardDraft): MutableList<String> {
        val value = item.endsAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("ends at $value is below $min")
        }
        if (value > max) {
            problems.add("ends at $value is above $max")
        }
        return problems
    }
}

data class BatchedTrailerYardDraftLabelLengthRule(var maxLength: Int) : BatchedTrailerYardDraftRule {
    override fun check(item: BatchedTrailerYardDraft): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class BatchedTrailerYardDraftRequiredCodeRule(var message: String) : BatchedTrailerYardDraftRule {
    override fun check(item: BatchedTrailerYardDraft): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a batched trailer yard draft. */
data class BatchedTrailerYardDraftValidator(var rules: MutableList<BatchedTrailerYardDraftRule>) {
    fun validate(item: BatchedTrailerYardDraft): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: BatchedTrailerYardDraft) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<BatchedTrailerYardDraft>): MutableMap<String, MutableList<String>> {
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

fun defaultBatchedTrailerYardDraftValidator(): BatchedTrailerYardDraftValidator {
    return BatchedTrailerYardDraftValidator(
        mutableListOf<BatchedTrailerYardDraftRule>(
            BatchedTrailerYardDraftCostRangeRule(0.8, 2572.8),
            BatchedTrailerYardDraftCodeLengthRule(86400),
            BatchedTrailerYardDraftCodePrefixRule("REF"),
            BatchedTrailerYardDraftEndsAtRangeRule(64, 255),
            BatchedTrailerYardDraftLabelLengthRule(16),
            BatchedTrailerYardDraftRequiredCodeRule("batched trailer yard draft needs a value"),
        ),
    )
}
