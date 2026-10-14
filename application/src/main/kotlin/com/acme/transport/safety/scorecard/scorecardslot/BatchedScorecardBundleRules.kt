package com.acme.transport.safety.scorecard.scorecardslot

import com.acme.transport.kit.text.normalize

/** A rule a batched scorecard bundle must pass. Check returns its problems, or none. */
interface BatchedScorecardBundleRule {
    fun check(item: BatchedScorecardBundle): MutableList<String>
}

data class BatchedScorecardBundleMarginRangeRule(
    var min: Double,
    var max: Double,
) : BatchedScorecardBundleRule {
    override fun check(item: BatchedScorecardBundle): MutableList<String> {
        val value = item.margin
        if (value < min || value > max) {
            return mutableListOf<String>("margin $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class BatchedScorecardBundleCodeLengthRule(var maxLength: Int) : BatchedScorecardBundleRule {
    override fun check(item: BatchedScorecardBundle): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class BatchedScorecardBundleCodePrefixRule(var prefix: String) : BatchedScorecardBundleRule {
    override fun check(item: BatchedScorecardBundle): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class BatchedScorecardBundleTitleLengthRule(var maxLength: Int) : BatchedScorecardBundleRule {
    override fun check(item: BatchedScorecardBundle): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class BatchedScorecardBundleRequiredCodeRule(var message: String) : BatchedScorecardBundleRule {
    override fun check(item: BatchedScorecardBundle): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class BatchedScorecardBundleAllowedSegmentRule(
    var allowed: MutableList<String>,
) : BatchedScorecardBundleRule {
    override fun check(item: BatchedScorecardBundle): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.segment))) {
            return mutableListOf<String>("segment ${item.segment} is expired")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a batched scorecard bundle. */
data class BatchedScorecardBundleValidator(var rules: MutableList<BatchedScorecardBundleRule>) {
    fun validate(item: BatchedScorecardBundle): MutableList<String> {
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

    fun requireValid(item: BatchedScorecardBundle) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<BatchedScorecardBundle>): MutableMap<String, MutableList<String>> {
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

fun defaultBatchedScorecardBundleValidator(): BatchedScorecardBundleValidator {
    return BatchedScorecardBundleValidator(
        mutableListOf<BatchedScorecardBundleRule>(
            BatchedScorecardBundleMarginRangeRule(5.7, 3825.9),
            BatchedScorecardBundleCodeLengthRule(64),
            BatchedScorecardBundleCodePrefixRule("X"),
            BatchedScorecardBundleTitleLengthRule(86400),
            BatchedScorecardBundleRequiredCodeRule("batched scorecard bundle needs an entry"),
            BatchedScorecardBundleAllowedSegmentRule(mutableListOf<String>()),
        ),
    )
}
