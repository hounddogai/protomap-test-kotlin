package com.acme.transport.crewing.swap.claim

/** A rule a swap threshold must pass. Check returns its problems, or none. */
interface SwapThresholdRule {
    fun check(item: SwapThreshold): MutableList<String>
}

data class SwapThresholdRequiredSegmentRule(var message: String) : SwapThresholdRule {
    override fun check(item: SwapThreshold): MutableList<String> {
        if (item.segment.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class SwapThresholdRequiredChannelRule(var message: String) : SwapThresholdRule {
    override fun check(item: SwapThreshold): MutableList<String> {
        if (item.channel.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class SwapThresholdCodePrefixRule(var prefix: String) : SwapThresholdRule {
    override fun check(item: SwapThreshold): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a swap threshold. */
data class SwapThresholdValidator(var rules: MutableList<SwapThresholdRule>) {
    fun validate(item: SwapThreshold): MutableList<String> {
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

    fun requireValid(item: SwapThreshold) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<SwapThreshold>): MutableMap<String, MutableList<String>> {
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

fun defaultSwapThresholdValidator(): SwapThresholdValidator {
    return SwapThresholdValidator(
        mutableListOf<SwapThresholdRule>(
            SwapThresholdRequiredSegmentRule("swap threshold needs a value"),
            SwapThresholdRequiredChannelRule("swap threshold needs this field"),
            SwapThresholdCodePrefixRule("X"),
        ),
    )
}
