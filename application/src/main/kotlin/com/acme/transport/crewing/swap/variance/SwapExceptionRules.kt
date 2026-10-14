package com.acme.transport.crewing.swap.variance

import com.acme.transport.kit.text.normalize

/** A rule a swap exception must pass. Check returns its problems, or none. */
interface SwapExceptionRule {
    fun check(item: SwapException): MutableList<String>
}

data class SwapExceptionAllowedDriverIdRule(var allowed: MutableList<String>) : SwapExceptionRule {
    override fun check(item: SwapException): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.driverId))) {
            return mutableListOf<String>("driver id ${item.driverId} is expired")
        }
        return mutableListOf<String>()
    }
}

data class SwapExceptionDriverIdLengthRule(var maxLength: Int) : SwapExceptionRule {
    override fun check(item: SwapException): MutableList<String> {
        val size = item.driverId.length
        if (size > maxLength) {
            return mutableListOf<String>("driver id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class SwapExceptionCodePrefixRule(var prefix: String) : SwapExceptionRule {
    override fun check(item: SwapException): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a swap exception. */
data class SwapExceptionValidator(var rules: MutableList<SwapExceptionRule>) {
    fun validate(item: SwapException): MutableList<String> {
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

    fun requireValid(item: SwapException) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<SwapException>): MutableMap<String, MutableList<String>> {
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

fun defaultSwapExceptionValidator(): SwapExceptionValidator {
    return SwapExceptionValidator(
        mutableListOf<SwapExceptionRule>(
            SwapExceptionAllowedDriverIdRule(mutableListOf<String>("strict", "manual")),
            SwapExceptionDriverIdLengthRule(1000),
            SwapExceptionCodePrefixRule("Q"),
        ),
    )
}
