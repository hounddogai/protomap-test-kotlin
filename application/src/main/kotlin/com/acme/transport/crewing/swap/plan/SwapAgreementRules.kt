package com.acme.transport.crewing.swap.plan

import com.acme.transport.kit.text.normalize

/** A rule a swap agreement must pass. Check returns its problems, or none. */
interface SwapAgreementRule {
    fun check(item: SwapAgreement): MutableList<String>
}

data class SwapAgreementRequiredLabelRule(var message: String) : SwapAgreementRule {
    override fun check(item: SwapAgreement): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class SwapAgreementAllowedCodeRule(var allowed: MutableList<String>) : SwapAgreementRule {
    override fun check(item: SwapAgreement): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is inconsistent")
        }
        return mutableListOf<String>()
    }
}

data class SwapAgreementSequenceRangeRule(var min: Int, var max: Int) : SwapAgreementRule {
    override fun check(item: SwapAgreement): MutableList<String> {
        val value = item.sequence
        if (value < min || value > max) {
            return mutableListOf<String>("sequence $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a swap agreement. */
data class SwapAgreementValidator(var rules: MutableList<SwapAgreementRule>) {
    fun validate(item: SwapAgreement): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: SwapAgreement) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<SwapAgreement>): MutableMap<String, MutableList<String>> {
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

fun defaultSwapAgreementValidator(): SwapAgreementValidator {
    return SwapAgreementValidator(
        mutableListOf<SwapAgreementRule>(
            SwapAgreementRequiredLabelRule("swap agreement needs this field"),
            SwapAgreementAllowedCodeRule(mutableListOf<String>("weighted", "capped", "default")),
            SwapAgreementSequenceRangeRule(255, 1000),
        ),
    )
}
