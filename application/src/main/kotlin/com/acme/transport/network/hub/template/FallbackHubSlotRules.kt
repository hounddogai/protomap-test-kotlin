package com.acme.transport.network.hub.template

/** A rule a fallback hub slot must pass. Check returns its problems, or none. */
interface FallbackHubSlotRule {
    fun check(item: FallbackHubSlot): MutableList<String>
}

data class FallbackHubSlotRetriesRangeRule(var min: Int, var max: Int) : FallbackHubSlotRule {
    override fun check(item: FallbackHubSlot): MutableList<String> {
        val value = item.retries
        if (value < min || value > max) {
            return mutableListOf<String>("retries $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class FallbackHubSlotCodePrefixRule(var prefix: String) : FallbackHubSlotRule {
    override fun check(item: FallbackHubSlot): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class FallbackHubSlotChecksumLengthRule(var maxLength: Int) : FallbackHubSlotRule {
    override fun check(item: FallbackHubSlot): MutableList<String> {
        val size = item.checksum.length
        if (size > maxLength) {
            return mutableListOf<String>("checksum is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a fallback hub slot. */
data class FallbackHubSlotValidator(var rules: MutableList<FallbackHubSlotRule>) {
    fun validate(item: FallbackHubSlot): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: FallbackHubSlot) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FallbackHubSlot>): MutableMap<String, MutableList<String>> {
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

fun defaultFallbackHubSlotValidator(): FallbackHubSlotValidator {
    return FallbackHubSlotValidator(
        mutableListOf<FallbackHubSlotRule>(
            FallbackHubSlotRetriesRangeRule(16, 64),
            FallbackHubSlotCodePrefixRule("OPS"),
            FallbackHubSlotChecksumLengthRule(3600),
        ),
    )
}
