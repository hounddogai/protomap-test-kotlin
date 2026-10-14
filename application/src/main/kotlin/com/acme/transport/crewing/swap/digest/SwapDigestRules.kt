package com.acme.transport.crewing.swap.digest

import com.acme.transport.kit.text.normalize

/** A rule a swap digest must pass. Check returns its problems, or none. */
interface SwapDigestRule {
    fun check(item: SwapDigest): MutableList<String>
}

data class SwapDigestAllowedExternalRefRule(var allowed: MutableList<String>) : SwapDigestRule {
    override fun check(item: SwapDigest): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.externalRef))) {
            return mutableListOf<String>("external ref ${item.externalRef} is inconsistent")
        }
        return mutableListOf<String>()
    }
}

data class SwapDigestCodeLengthRule(var maxLength: Int) : SwapDigestRule {
    override fun check(item: SwapDigest): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class SwapDigestLabelLengthRule(var maxLength: Int) : SwapDigestRule {
    override fun check(item: SwapDigest): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class SwapDigestCarrierLengthRule(var maxLength: Int) : SwapDigestRule {
    override fun check(item: SwapDigest): MutableList<String> {
        val size = item.carrier.length
        if (size > maxLength) {
            return mutableListOf<String>("carrier is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a swap digest. */
data class SwapDigestValidator(var rules: MutableList<SwapDigestRule>) {
    fun validate(item: SwapDigest): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: SwapDigest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<SwapDigest>): MutableMap<String, MutableList<String>> {
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

fun defaultSwapDigestValidator(): SwapDigestValidator {
    return SwapDigestValidator(
        mutableListOf<SwapDigestRule>(
            SwapDigestAllowedExternalRefRule(mutableListOf<String>()),
            SwapDigestCodeLengthRule(1000),
            SwapDigestLabelLengthRule(16),
            SwapDigestCarrierLengthRule(1000),
        ),
    )
}
