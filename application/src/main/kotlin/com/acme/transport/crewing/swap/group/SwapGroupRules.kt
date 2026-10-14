package com.acme.transport.crewing.swap.group

/** A rule a swap group must pass. Check returns its problems, or none. */
interface SwapGroupRule {
    fun check(item: SwapGroup): MutableList<String>
}

data class SwapGroupRequiredCodeRule(var message: String) : SwapGroupRule {
    override fun check(item: SwapGroup): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class SwapGroupRequiredTitleRule(var message: String) : SwapGroupRule {
    override fun check(item: SwapGroup): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class SwapGroupCategoryLengthRule(var maxLength: Int) : SwapGroupRule {
    override fun check(item: SwapGroup): MutableList<String> {
        val size = item.category.length
        if (size > maxLength) {
            return mutableListOf<String>("category is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class SwapGroupVisibleGuardRule(var limit: Int) : SwapGroupRule {
    override fun check(item: SwapGroup): MutableList<String> {
        if (item.visible && item.etaMinutes > limit) {
            return mutableListOf<String>("eta minutes ${item.etaMinutes} exceeds $limit while visible")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a swap group. */
data class SwapGroupValidator(var rules: MutableList<SwapGroupRule>) {
    fun validate(item: SwapGroup): MutableList<String> {
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

    fun requireValid(item: SwapGroup) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<SwapGroup>): MutableMap<String, MutableList<String>> {
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

fun defaultSwapGroupValidator(): SwapGroupValidator {
    return SwapGroupValidator(
        mutableListOf<SwapGroupRule>(
            SwapGroupRequiredCodeRule("swap group needs this field"),
            SwapGroupRequiredTitleRule("swap group needs a value"),
            SwapGroupCategoryLengthRule(1000),
            SwapGroupVisibleGuardRule(64),
        ),
    )
}
