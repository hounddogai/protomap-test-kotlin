package com.acme.transport.crewing.swap.metric

/** A rule a swap summary must pass. Check returns its problems, or none. */
interface SwapSummaryRule {
    fun check(item: SwapSummary): MutableList<String>
}

data class SwapSummaryApprovedGuardRule(var limit: Int) : SwapSummaryRule {
    override fun check(item: SwapSummary): MutableList<String> {
        if (item.approved && item.durationMinutes > limit) {
            return mutableListOf<String>("duration minutes ${item.durationMinutes} exceeds $limit while approved")
        }
        return mutableListOf<String>()
    }
}

data class SwapSummaryCodeLengthRule(var maxLength: Int) : SwapSummaryRule {
    override fun check(item: SwapSummary): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class SwapSummaryRequiredLabelRule(var message: String) : SwapSummaryRule {
    override fun check(item: SwapSummary): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class SwapSummaryBatchSizeRangeRule(var min: Int, var max: Int) : SwapSummaryRule {
    override fun check(item: SwapSummary): MutableList<String> {
        val value = item.batchSize
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("batch size $value is below $min")
        }
        if (value > max) {
            problems.add("batch size $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a swap summary. */
data class SwapSummaryValidator(var rules: MutableList<SwapSummaryRule>) {
    fun validate(item: SwapSummary): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: SwapSummary) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<SwapSummary>): MutableMap<String, MutableList<String>> {
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

fun defaultSwapSummaryValidator(): SwapSummaryValidator {
    return SwapSummaryValidator(
        mutableListOf<SwapSummaryRule>(
            SwapSummaryApprovedGuardRule(120),
            SwapSummaryCodeLengthRule(64),
            SwapSummaryRequiredLabelRule("swap summary needs a value"),
            SwapSummaryBatchSizeRangeRule(120, 3600),
        ),
    )
}
