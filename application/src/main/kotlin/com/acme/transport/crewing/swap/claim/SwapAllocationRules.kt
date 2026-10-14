package com.acme.transport.crewing.swap.claim

import com.acme.transport.kit.text.normalize

/** A rule a swap allocation must pass. Check returns its problems, or none. */
interface SwapAllocationRule {
    fun check(item: SwapAllocation): MutableList<String>
}

data class SwapAllocationRequiredTitleRule(var message: String) : SwapAllocationRule {
    override fun check(item: SwapAllocation): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class SwapAllocationCodePrefixRule(var prefix: String) : SwapAllocationRule {
    override fun check(item: SwapAllocation): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class SwapAllocationRequiredCodeRule(var message: String) : SwapAllocationRule {
    override fun check(item: SwapAllocation): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class SwapAllocationCodeLengthRule(var maxLength: Int) : SwapAllocationRule {
    override fun check(item: SwapAllocation): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class SwapAllocationAllowedTitleRule(var allowed: MutableList<String>) : SwapAllocationRule {
    override fun check(item: SwapAllocation): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.title))) {
            return mutableListOf<String>("title ${item.title} is below minimum")
        }
        return mutableListOf<String>()
    }
}

data class SwapAllocationShiftCodeLengthRule(var maxLength: Int) : SwapAllocationRule {
    override fun check(item: SwapAllocation): MutableList<String> {
        val size = item.shiftCode.length
        if (size > maxLength) {
            return mutableListOf<String>("shift code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a swap allocation. */
data class SwapAllocationValidator(var rules: MutableList<SwapAllocationRule>) {
    fun validate(item: SwapAllocation): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: SwapAllocation) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<SwapAllocation>): MutableMap<String, MutableList<String>> {
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

fun defaultSwapAllocationValidator(): SwapAllocationValidator {
    return SwapAllocationValidator(
        mutableListOf<SwapAllocationRule>(
            SwapAllocationRequiredTitleRule("swap allocation needs an entry"),
            SwapAllocationCodePrefixRule("X"),
            SwapAllocationRequiredCodeRule("swap allocation needs a value"),
            SwapAllocationCodeLengthRule(80),
            SwapAllocationAllowedTitleRule(mutableListOf<String>("tiered", "rolling")),
            SwapAllocationShiftCodeLengthRule(80),
        ),
    )
}
