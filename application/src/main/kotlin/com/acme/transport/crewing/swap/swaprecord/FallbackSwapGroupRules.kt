package com.acme.transport.crewing.swap.swaprecord

/** A rule a fallback swap group must pass. Check returns its problems, or none. */
interface FallbackSwapGroupRule {
    fun check(item: FallbackSwapGroup): MutableList<String>
}

data class FallbackSwapGroupLabelLengthRule(var maxLength: Int) : FallbackSwapGroupRule {
    override fun check(item: FallbackSwapGroup): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FallbackSwapGroupRequiredLabelRule(var message: String) : FallbackSwapGroupRule {
    override fun check(item: FallbackSwapGroup): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FallbackSwapGroupCodePrefixRule(var prefix: String) : FallbackSwapGroupRule {
    override fun check(item: FallbackSwapGroup): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class FallbackSwapGroupTitleLengthRule(var maxLength: Int) : FallbackSwapGroupRule {
    override fun check(item: FallbackSwapGroup): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a fallback swap group. */
data class FallbackSwapGroupValidator(var rules: MutableList<FallbackSwapGroupRule>) {
    fun validate(item: FallbackSwapGroup): MutableList<String> {
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

    fun requireValid(item: FallbackSwapGroup) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FallbackSwapGroup>): MutableMap<String, MutableList<String>> {
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

fun defaultFallbackSwapGroupValidator(): FallbackSwapGroupValidator {
    return FallbackSwapGroupValidator(
        mutableListOf<FallbackSwapGroupRule>(
            FallbackSwapGroupLabelLengthRule(120),
            FallbackSwapGroupRequiredLabelRule("fallback swap group needs an entry"),
            FallbackSwapGroupCodePrefixRule("OPS"),
            FallbackSwapGroupTitleLengthRule(86400),
        ),
    )
}
