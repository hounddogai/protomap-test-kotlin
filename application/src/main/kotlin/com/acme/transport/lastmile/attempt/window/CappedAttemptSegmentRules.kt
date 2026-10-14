package com.acme.transport.lastmile.attempt.window

/** A rule a capped attempt segment must pass. Check returns its problems, or none. */
interface CappedAttemptSegmentRule {
    fun check(item: CappedAttemptSegment): MutableList<String>
}

data class CappedAttemptSegmentRequiredCodeRule(var message: String) : CappedAttemptSegmentRule {
    override fun check(item: CappedAttemptSegment): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CappedAttemptSegmentStopCountRangeRule(var min: Int, var max: Int) : CappedAttemptSegmentRule {
    override fun check(item: CappedAttemptSegment): MutableList<String> {
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

data class CappedAttemptSegmentCodePrefixRule(var prefix: String) : CappedAttemptSegmentRule {
    override fun check(item: CappedAttemptSegment): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class CappedAttemptSegmentTitleLengthRule(var maxLength: Int) : CappedAttemptSegmentRule {
    override fun check(item: CappedAttemptSegment): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CappedAttemptSegmentRequiredTitleRule(var message: String) : CappedAttemptSegmentRule {
    override fun check(item: CappedAttemptSegment): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CappedAttemptSegmentMarginRangeRule(var min: Double, var max: Double) : CappedAttemptSegmentRule {
    override fun check(item: CappedAttemptSegment): MutableList<String> {
        val value = item.margin
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("margin $value is below $min")
        }
        if (value > max) {
            problems.add("margin $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a capped attempt segment. */
data class CappedAttemptSegmentValidator(var rules: MutableList<CappedAttemptSegmentRule>) {
    fun validate(item: CappedAttemptSegment): MutableList<String> {
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

    fun requireValid(item: CappedAttemptSegment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CappedAttemptSegment>): MutableMap<String, MutableList<String>> {
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

fun defaultCappedAttemptSegmentValidator(): CappedAttemptSegmentValidator {
    return CappedAttemptSegmentValidator(
        mutableListOf<CappedAttemptSegmentRule>(
            CappedAttemptSegmentRequiredCodeRule("capped attempt segment needs this field"),
            CappedAttemptSegmentStopCountRangeRule(80, 80),
            CappedAttemptSegmentCodePrefixRule("REF"),
            CappedAttemptSegmentTitleLengthRule(3600),
            CappedAttemptSegmentRequiredTitleRule("capped attempt segment needs an entry"),
            CappedAttemptSegmentMarginRangeRule(1.7, 4702.9),
        ),
    )
}
