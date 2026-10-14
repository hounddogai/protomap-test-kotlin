package com.acme.transport.crewing.swap.draft

/** A rule a swap metric must pass. Check returns its problems, or none. */
interface SwapMetricRule {
    fun check(item: SwapMetric): MutableList<String>
}

data class SwapMetricRequiredTitleRule(var message: String) : SwapMetricRule {
    override fun check(item: SwapMetric): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class SwapMetricCodePrefixRule(var prefix: String) : SwapMetricRule {
    override fun check(item: SwapMetric): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class SwapMetricTitleLengthRule(var maxLength: Int) : SwapMetricRule {
    override fun check(item: SwapMetric): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a swap metric. */
data class SwapMetricValidator(var rules: MutableList<SwapMetricRule>) {
    fun validate(item: SwapMetric): MutableList<String> {
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

    fun requireValid(item: SwapMetric) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<SwapMetric>): MutableMap<String, MutableList<String>> {
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

fun defaultSwapMetricValidator(): SwapMetricValidator {
    return SwapMetricValidator(
        mutableListOf<SwapMetricRule>(
            SwapMetricRequiredTitleRule("swap metric needs this field"),
            SwapMetricCodePrefixRule("REF"),
            SwapMetricTitleLengthRule(3600),
        ),
    )
}
