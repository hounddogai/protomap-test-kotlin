package com.acme.transport.safety.nearmiss.variance

/** A rule a batched near miss segment must pass. Check returns its problems, or none. */
interface BatchedNearMissSegmentRule {
    fun check(item: BatchedNearMissSegment): MutableList<String>
}

data class BatchedNearMissSegmentLimitRangeRule(var min: Int, var max: Int) : BatchedNearMissSegmentRule {
    override fun check(item: BatchedNearMissSegment): MutableList<String> {
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

data class BatchedNearMissSegmentCodePrefixRule(var prefix: String) : BatchedNearMissSegmentRule {
    override fun check(item: BatchedNearMissSegment): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class BatchedNearMissSegmentRequiredLocaleRule(var message: String) : BatchedNearMissSegmentRule {
    override fun check(item: BatchedNearMissSegment): MutableList<String> {
        if (item.locale.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a batched near miss segment. */
data class BatchedNearMissSegmentValidator(var rules: MutableList<BatchedNearMissSegmentRule>) {
    fun validate(item: BatchedNearMissSegment): MutableList<String> {
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

    fun requireValid(item: BatchedNearMissSegment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<BatchedNearMissSegment>): MutableMap<String, MutableList<String>> {
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

fun defaultBatchedNearMissSegmentValidator(): BatchedNearMissSegmentValidator {
    return BatchedNearMissSegmentValidator(
        mutableListOf<BatchedNearMissSegmentRule>(
            BatchedNearMissSegmentLimitRangeRule(32, 64),
            BatchedNearMissSegmentCodePrefixRule("X"),
            BatchedNearMissSegmentRequiredLocaleRule("batched near miss segment needs this field"),
        ),
    )
}
