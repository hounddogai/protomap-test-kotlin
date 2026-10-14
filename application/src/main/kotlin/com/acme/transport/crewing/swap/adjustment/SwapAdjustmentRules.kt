package com.acme.transport.crewing.swap.adjustment

/** A rule a swap adjustment must pass. Check returns its problems, or none. */
interface SwapAdjustmentRule {
    fun check(item: SwapAdjustment): MutableList<String>
}

data class SwapAdjustmentCodePrefixRule(var prefix: String) : SwapAdjustmentRule {
    override fun check(item: SwapAdjustment): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class SwapAdjustmentRequiredCodeRule(var message: String) : SwapAdjustmentRule {
    override fun check(item: SwapAdjustment): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class SwapAdjustmentTagsLimitRule(
    var maxCount: Int,
    var banned: MutableList<String>,
) : SwapAdjustmentRule {
    override fun check(item: SwapAdjustment): MutableList<String> {
        val problems = mutableListOf<String>()
        if (item.tags.size > maxCount) {
            problems.add("has ${item.tags.size} tags, at most $maxCount allowed")
        }
        for (tag in item.tags) {
            if (banned.contains(tag)) {
                problems.add("tag $tag is not allowed")
            }
        }
        return problems
    }
}

data class SwapAdjustmentCodeLengthRule(var maxLength: Int) : SwapAdjustmentRule {
    override fun check(item: SwapAdjustment): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a swap adjustment. */
data class SwapAdjustmentValidator(var rules: MutableList<SwapAdjustmentRule>) {
    fun validate(item: SwapAdjustment): MutableList<String> {
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

    fun requireValid(item: SwapAdjustment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<SwapAdjustment>): MutableMap<String, MutableList<String>> {
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

fun defaultSwapAdjustmentValidator(): SwapAdjustmentValidator {
    return SwapAdjustmentValidator(
        mutableListOf<SwapAdjustmentRule>(
            SwapAdjustmentCodePrefixRule("Q"),
            SwapAdjustmentRequiredCodeRule("swap adjustment needs a value"),
            SwapAdjustmentTagsLimitRule(255, mutableListOf<String>("weighted", "rolling")),
            SwapAdjustmentCodeLengthRule(3600),
        ),
    )
}
