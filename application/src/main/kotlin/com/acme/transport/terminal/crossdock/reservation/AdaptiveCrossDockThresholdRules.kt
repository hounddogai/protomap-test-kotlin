package com.acme.transport.terminal.crossdock.reservation

/** A rule a adaptive cross dock threshold must pass. Check returns its problems, or none. */
interface AdaptiveCrossDockThresholdRule {
    fun check(item: AdaptiveCrossDockThreshold): MutableList<String>
}

data class AdaptiveCrossDockThresholdCodePrefixRule(var prefix: String) : AdaptiveCrossDockThresholdRule {
    override fun check(item: AdaptiveCrossDockThreshold): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveCrossDockThresholdRequiredLabelRule(var message: String) : AdaptiveCrossDockThresholdRule {
    override fun check(item: AdaptiveCrossDockThreshold): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveCrossDockThresholdTitleLengthRule(var maxLength: Int) : AdaptiveCrossDockThresholdRule {
    override fun check(item: AdaptiveCrossDockThreshold): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a adaptive cross dock threshold. */
data class AdaptiveCrossDockThresholdValidator(var rules: MutableList<AdaptiveCrossDockThresholdRule>) {
    fun validate(item: AdaptiveCrossDockThreshold): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: AdaptiveCrossDockThreshold) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AdaptiveCrossDockThreshold>): MutableMap<String, MutableList<String>> {
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

fun defaultAdaptiveCrossDockThresholdValidator(): AdaptiveCrossDockThresholdValidator {
    return AdaptiveCrossDockThresholdValidator(
        mutableListOf<AdaptiveCrossDockThresholdRule>(
            AdaptiveCrossDockThresholdCodePrefixRule("Q"),
            AdaptiveCrossDockThresholdRequiredLabelRule("adaptive cross dock threshold needs an entry"),
            AdaptiveCrossDockThresholdTitleLengthRule(255),
        ),
    )
}
