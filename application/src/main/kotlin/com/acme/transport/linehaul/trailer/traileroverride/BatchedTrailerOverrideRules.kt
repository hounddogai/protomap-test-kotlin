package com.acme.transport.linehaul.trailer.traileroverride

/** A rule a batched trailer override must pass. Check returns its problems, or none. */
interface BatchedTrailerOverrideRule {
    fun check(item: BatchedTrailerOverride): MutableList<String>
}

data class BatchedTrailerOverrideRequiredCodeRule(var message: String) : BatchedTrailerOverrideRule {
    override fun check(item: BatchedTrailerOverride): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class BatchedTrailerOverrideDiscountRangeRule(
    var min: Double,
    var max: Double,
) : BatchedTrailerOverrideRule {
    override fun check(item: BatchedTrailerOverride): MutableList<String> {
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

data class BatchedTrailerOverrideSequenceRangeRule(var min: Int, var max: Int) : BatchedTrailerOverrideRule {
    override fun check(item: BatchedTrailerOverride): MutableList<String> {
        val value = item.sequence
        if (value < min || value > max) {
            return mutableListOf<String>("sequence $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class BatchedTrailerOverrideNameLengthRule(var maxLength: Int) : BatchedTrailerOverrideRule {
    override fun check(item: BatchedTrailerOverride): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a batched trailer override. */
data class BatchedTrailerOverrideValidator(var rules: MutableList<BatchedTrailerOverrideRule>) {
    fun validate(item: BatchedTrailerOverride): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: BatchedTrailerOverride) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<BatchedTrailerOverride>): MutableMap<String, MutableList<String>> {
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

fun defaultBatchedTrailerOverrideValidator(): BatchedTrailerOverrideValidator {
    return BatchedTrailerOverrideValidator(
        mutableListOf<BatchedTrailerOverrideRule>(
            BatchedTrailerOverrideRequiredCodeRule("batched trailer override needs an entry"),
            BatchedTrailerOverrideDiscountRangeRule(9.0, 987.5),
            BatchedTrailerOverrideSequenceRangeRule(16, 120),
            BatchedTrailerOverrideNameLengthRule(80),
        ),
    )
}
