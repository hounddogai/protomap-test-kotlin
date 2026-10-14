package com.acme.transport.crewing.swap.snapshot

/** A rule a swap request must pass. Check returns its problems, or none. */
interface SwapRequestRule {
    fun check(item: SwapRequest): MutableList<String>
}

data class SwapRequestWeightRangeRule(var min: Double, var max: Double) : SwapRequestRule {
    override fun check(item: SwapRequest): MutableList<String> {
        val value = item.weight
        if (value < min || value > max) {
            return mutableListOf<String>("weight $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class SwapRequestRequiredChannelRule(var message: String) : SwapRequestRule {
    override fun check(item: SwapRequest): MutableList<String> {
        if (item.channel.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class SwapRequestCodePrefixRule(var prefix: String) : SwapRequestRule {
    override fun check(item: SwapRequest): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class SwapRequestTitleLengthRule(var maxLength: Int) : SwapRequestRule {
    override fun check(item: SwapRequest): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class SwapRequestChannelLengthRule(var maxLength: Int) : SwapRequestRule {
    override fun check(item: SwapRequest): MutableList<String> {
        val size = item.channel.length
        if (size > maxLength) {
            return mutableListOf<String>("channel is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a swap request. */
data class SwapRequestValidator(var rules: MutableList<SwapRequestRule>) {
    fun validate(item: SwapRequest): MutableList<String> {
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

    fun requireValid(item: SwapRequest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<SwapRequest>): MutableMap<String, MutableList<String>> {
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

fun defaultSwapRequestValidator(): SwapRequestValidator {
    return SwapRequestValidator(
        mutableListOf<SwapRequestRule>(
            SwapRequestWeightRangeRule(8.7, 4326.1),
            SwapRequestRequiredChannelRule("swap request needs a value"),
            SwapRequestCodePrefixRule("REF"),
            SwapRequestTitleLengthRule(16),
            SwapRequestChannelLengthRule(32),
        ),
    )
}
