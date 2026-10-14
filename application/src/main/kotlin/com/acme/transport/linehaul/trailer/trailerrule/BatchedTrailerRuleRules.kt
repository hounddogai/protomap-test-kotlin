package com.acme.transport.linehaul.trailer.trailerrule

/** A rule a batched trailer rule must pass. Check returns its problems, or none. */
interface BatchedTrailerRuleRule {
    fun check(item: BatchedTrailerRule): MutableList<String>
}

data class BatchedTrailerRuleStopCountRangeRule(var min: Int, var max: Int) : BatchedTrailerRuleRule {
    override fun check(item: BatchedTrailerRule): MutableList<String> {
        val value = item.stopCount
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("stop count $value is below $min")
        }
        if (value > max) {
            problems.add("stop count $value is above $max")
        }
        return problems
    }
}

data class BatchedTrailerRuleCreatedAtRangeRule(var min: Int, var max: Int) : BatchedTrailerRuleRule {
    override fun check(item: BatchedTrailerRule): MutableList<String> {
        val value = item.createdAt
        if (value < min || value > max) {
            return mutableListOf<String>("created at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class BatchedTrailerRuleNotesLengthRule(var maxLength: Int) : BatchedTrailerRuleRule {
    override fun check(item: BatchedTrailerRule): MutableList<String> {
        val size = item.notes.length
        if (size > maxLength) {
            return mutableListOf<String>("notes is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class BatchedTrailerRuleRequiredCodeRule(var message: String) : BatchedTrailerRuleRule {
    override fun check(item: BatchedTrailerRule): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class BatchedTrailerRuleCodePrefixRule(var prefix: String) : BatchedTrailerRuleRule {
    override fun check(item: BatchedTrailerRule): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a batched trailer rule. */
data class BatchedTrailerRuleValidator(var rules: MutableList<BatchedTrailerRuleRule>) {
    fun validate(item: BatchedTrailerRule): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: BatchedTrailerRule) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<BatchedTrailerRule>): MutableMap<String, MutableList<String>> {
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

fun defaultBatchedTrailerRuleValidator(): BatchedTrailerRuleValidator {
    return BatchedTrailerRuleValidator(
        mutableListOf<BatchedTrailerRuleRule>(
            BatchedTrailerRuleStopCountRangeRule(32, 86400),
            BatchedTrailerRuleCreatedAtRangeRule(32, 120),
            BatchedTrailerRuleNotesLengthRule(80),
            BatchedTrailerRuleRequiredCodeRule("batched trailer rule needs this field"),
            BatchedTrailerRuleCodePrefixRule("OPS"),
        ),
    )
}
