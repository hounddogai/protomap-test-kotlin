package com.acme.transport.crewing.restbreak.breakentry

/** A rule a batched rest break threshold must pass. Check returns its problems, or none. */
interface BatchedRestBreakThresholdRule {
    fun check(item: BatchedRestBreakThreshold): MutableList<String>
}

data class BatchedRestBreakThresholdFloorValueRangeRule(
    var min: Double,
    var max: Double,
) : BatchedRestBreakThresholdRule {
    override fun check(item: BatchedRestBreakThreshold): MutableList<String> {
        val value = item.floorValue
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("floor value $value is below $min")
        }
        if (value > max) {
            problems.add("floor value $value is above $max")
        }
        return problems
    }
}

data class BatchedRestBreakThresholdEndsAtRangeRule(
    var min: Int,
    var max: Int,
) : BatchedRestBreakThresholdRule {
    override fun check(item: BatchedRestBreakThreshold): MutableList<String> {
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

data class BatchedRestBreakThresholdShiftCodeLengthRule(var maxLength: Int) : BatchedRestBreakThresholdRule {
    override fun check(item: BatchedRestBreakThreshold): MutableList<String> {
        val size = item.shiftCode.length
        if (size > maxLength) {
            return mutableListOf<String>("shift code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class BatchedRestBreakThresholdVisibleGuardRule(var limit: Int) : BatchedRestBreakThresholdRule {
    override fun check(item: BatchedRestBreakThreshold): MutableList<String> {
        if (item.visible && item.endsAt > limit) {
            return mutableListOf<String>("ends at ${item.endsAt} exceeds $limit while visible")
        }
        return mutableListOf<String>()
    }
}

data class BatchedRestBreakThresholdCodePrefixRule(var prefix: String) : BatchedRestBreakThresholdRule {
    override fun check(item: BatchedRestBreakThreshold): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class BatchedRestBreakThresholdCodeLengthRule(var maxLength: Int) : BatchedRestBreakThresholdRule {
    override fun check(item: BatchedRestBreakThreshold): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a batched rest break threshold. */
data class BatchedRestBreakThresholdValidator(var rules: MutableList<BatchedRestBreakThresholdRule>) {
    fun validate(item: BatchedRestBreakThreshold): MutableList<String> {
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

    fun requireValid(item: BatchedRestBreakThreshold) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<BatchedRestBreakThreshold>): MutableMap<String, MutableList<String>> {
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

fun defaultBatchedRestBreakThresholdValidator(): BatchedRestBreakThresholdValidator {
    return BatchedRestBreakThresholdValidator(
        mutableListOf<BatchedRestBreakThresholdRule>(
            BatchedRestBreakThresholdFloorValueRangeRule(3.6, 1415.9),
            BatchedRestBreakThresholdEndsAtRangeRule(120, 86400),
            BatchedRestBreakThresholdShiftCodeLengthRule(1000),
            BatchedRestBreakThresholdVisibleGuardRule(1000),
            BatchedRestBreakThresholdCodePrefixRule("Q"),
            BatchedRestBreakThresholdCodeLengthRule(64),
        ),
    )
}
