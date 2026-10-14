package com.acme.transport.crewing.swap.estimate

import com.acme.transport.kit.text.normalize

/** A rule a swap rollup must pass. Check returns its problems, or none. */
interface SwapRollupRule {
    fun check(item: SwapRollup): MutableList<String>
}

data class SwapRollupAllowedNameRule(var allowed: MutableList<String>) : SwapRollupRule {
    override fun check(item: SwapRollup): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.name))) {
            return mutableListOf<String>("name ${item.name} is too long")
        }
        return mutableListOf<String>()
    }
}

data class SwapRollupNameLengthRule(var maxLength: Int) : SwapRollupRule {
    override fun check(item: SwapRollup): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class SwapRollupCodeLengthRule(var maxLength: Int) : SwapRollupRule {
    override fun check(item: SwapRollup): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class SwapRollupRequiredTrailerIdRule(var message: String) : SwapRollupRule {
    override fun check(item: SwapRollup): MutableList<String> {
        if (item.trailerId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a swap rollup. */
data class SwapRollupValidator(var rules: MutableList<SwapRollupRule>) {
    fun validate(item: SwapRollup): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: SwapRollup) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<SwapRollup>): MutableMap<String, MutableList<String>> {
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

fun defaultSwapRollupValidator(): SwapRollupValidator {
    return SwapRollupValidator(
        mutableListOf<SwapRollupRule>(
            SwapRollupAllowedNameRule(mutableListOf<String>("weighted")),
            SwapRollupNameLengthRule(86400),
            SwapRollupCodeLengthRule(255),
            SwapRollupRequiredTrailerIdRule("swap rollup needs this field"),
        ),
    )
}
