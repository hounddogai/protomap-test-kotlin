package com.acme.transport.linehaul.trailer.summary

/** A rule a adaptive trailer summary must pass. Check returns its problems, or none. */
interface AdaptiveTrailerSummaryRule {
    fun check(item: AdaptiveTrailerSummary): MutableList<String>
}

data class AdaptiveTrailerSummaryCodeLengthRule(var maxLength: Int) : AdaptiveTrailerSummaryRule {
    override fun check(item: AdaptiveTrailerSummary): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveTrailerSummaryNameLengthRule(var maxLength: Int) : AdaptiveTrailerSummaryRule {
    override fun check(item: AdaptiveTrailerSummary): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveTrailerSummaryCodePrefixRule(var prefix: String) : AdaptiveTrailerSummaryRule {
    override fun check(item: AdaptiveTrailerSummary): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveTrailerSummaryCeilingRangeRule(
    var min: Double,
    var max: Double,
) : AdaptiveTrailerSummaryRule {
    override fun check(item: AdaptiveTrailerSummary): MutableList<String> {
        val value = item.ceiling
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("ceiling $value is below $min")
        }
        if (value > max) {
            problems.add("ceiling $value is above $max")
        }
        return problems
    }
}

data class AdaptiveTrailerSummaryChannelLengthRule(var maxLength: Int) : AdaptiveTrailerSummaryRule {
    override fun check(item: AdaptiveTrailerSummary): MutableList<String> {
        val size = item.channel.length
        if (size > maxLength) {
            return mutableListOf<String>("channel is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a adaptive trailer summary. */
data class AdaptiveTrailerSummaryValidator(var rules: MutableList<AdaptiveTrailerSummaryRule>) {
    fun validate(item: AdaptiveTrailerSummary): MutableList<String> {
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

    fun requireValid(item: AdaptiveTrailerSummary) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AdaptiveTrailerSummary>): MutableMap<String, MutableList<String>> {
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

fun defaultAdaptiveTrailerSummaryValidator(): AdaptiveTrailerSummaryValidator {
    return AdaptiveTrailerSummaryValidator(
        mutableListOf<AdaptiveTrailerSummaryRule>(
            AdaptiveTrailerSummaryCodeLengthRule(3600),
            AdaptiveTrailerSummaryNameLengthRule(255),
            AdaptiveTrailerSummaryCodePrefixRule("Q"),
            AdaptiveTrailerSummaryCeilingRangeRule(0.8, 2864.3),
            AdaptiveTrailerSummaryChannelLengthRule(3600),
        ),
    )
}
