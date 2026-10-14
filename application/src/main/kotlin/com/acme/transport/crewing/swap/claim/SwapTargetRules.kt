package com.acme.transport.crewing.swap.claim

import com.acme.transport.kit.text.normalize

/** A rule a swap target must pass. Check returns its problems, or none. */
interface SwapTargetRule {
    fun check(item: SwapTarget): MutableList<String>
}

data class SwapTargetReviewerLengthRule(var maxLength: Int) : SwapTargetRule {
    override fun check(item: SwapTarget): MutableList<String> {
        val size = item.reviewer.length
        if (size > maxLength) {
            return mutableListOf<String>("reviewer is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class SwapTargetAllowedCodeRule(var allowed: MutableList<String>) : SwapTargetRule {
    override fun check(item: SwapTarget): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is inconsistent")
        }
        return mutableListOf<String>()
    }
}

data class SwapTargetNameLengthRule(var maxLength: Int) : SwapTargetRule {
    override fun check(item: SwapTarget): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class SwapTargetTrailerIdLengthRule(var maxLength: Int) : SwapTargetRule {
    override fun check(item: SwapTarget): MutableList<String> {
        val size = item.trailerId.length
        if (size > maxLength) {
            return mutableListOf<String>("trailer id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class SwapTargetBudgetRangeRule(var min: Double, var max: Double) : SwapTargetRule {
    override fun check(item: SwapTarget): MutableList<String> {
        val value = item.budget
        if (value < min || value > max) {
            return mutableListOf<String>("budget $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a swap target. */
data class SwapTargetValidator(var rules: MutableList<SwapTargetRule>) {
    fun validate(item: SwapTarget): MutableList<String> {
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

    fun requireValid(item: SwapTarget) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<SwapTarget>): MutableMap<String, MutableList<String>> {
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

fun defaultSwapTargetValidator(): SwapTargetValidator {
    return SwapTargetValidator(
        mutableListOf<SwapTargetRule>(
            SwapTargetReviewerLengthRule(255),
            SwapTargetAllowedCodeRule(mutableListOf<String>("legacy")),
            SwapTargetNameLengthRule(80),
            SwapTargetTrailerIdLengthRule(3600),
            SwapTargetBudgetRangeRule(5.1, 4867.0),
        ),
    )
}
