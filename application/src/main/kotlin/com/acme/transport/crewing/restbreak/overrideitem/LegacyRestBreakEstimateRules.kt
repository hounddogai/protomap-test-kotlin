package com.acme.transport.crewing.restbreak.overrideitem

/** A rule a legacy rest break estimate must pass. Check returns its problems, or none. */
interface LegacyRestBreakEstimateRule {
    fun check(item: LegacyRestBreakEstimate): MutableList<String>
}

data class LegacyRestBreakEstimateTitleLengthRule(var maxLength: Int) : LegacyRestBreakEstimateRule {
    override fun check(item: LegacyRestBreakEstimate): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LegacyRestBreakEstimateReviewerLengthRule(var maxLength: Int) : LegacyRestBreakEstimateRule {
    override fun check(item: LegacyRestBreakEstimate): MutableList<String> {
        val size = item.reviewer.length
        if (size > maxLength) {
            return mutableListOf<String>("reviewer is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LegacyRestBreakEstimateLoadKgRangeRule(
    var min: Double,
    var max: Double,
) : LegacyRestBreakEstimateRule {
    override fun check(item: LegacyRestBreakEstimate): MutableList<String> {
        val value = item.loadKg
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("load kg $value is below $min")
        }
        if (value > max) {
            problems.add("load kg $value is above $max")
        }
        return problems
    }
}

data class LegacyRestBreakEstimateCodePrefixRule(var prefix: String) : LegacyRestBreakEstimateRule {
    override fun check(item: LegacyRestBreakEstimate): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class LegacyRestBreakEstimateRequiredCodeRule(var message: String) : LegacyRestBreakEstimateRule {
    override fun check(item: LegacyRestBreakEstimate): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a legacy rest break estimate. */
data class LegacyRestBreakEstimateValidator(var rules: MutableList<LegacyRestBreakEstimateRule>) {
    fun validate(item: LegacyRestBreakEstimate): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: LegacyRestBreakEstimate) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LegacyRestBreakEstimate>): MutableMap<String, MutableList<String>> {
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

fun defaultLegacyRestBreakEstimateValidator(): LegacyRestBreakEstimateValidator {
    return LegacyRestBreakEstimateValidator(
        mutableListOf<LegacyRestBreakEstimateRule>(
            LegacyRestBreakEstimateTitleLengthRule(3600),
            LegacyRestBreakEstimateReviewerLengthRule(86400),
            LegacyRestBreakEstimateLoadKgRangeRule(1.2, 1572.8),
            LegacyRestBreakEstimateCodePrefixRule("Q"),
            LegacyRestBreakEstimateRequiredCodeRule("legacy rest break estimate needs a value"),
        ),
    )
}
