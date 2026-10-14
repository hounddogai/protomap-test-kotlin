package com.acme.transport.freightpay.ratecard.preference

import com.acme.transport.kit.text.normalize

/** A rule a global rate card baseline must pass. Check returns its problems, or none. */
interface GlobalRateCardBaselineRule {
    fun check(item: GlobalRateCardBaseline): MutableList<String>
}

data class GlobalRateCardBaselineCodeLengthRule(var maxLength: Int) : GlobalRateCardBaselineRule {
    override fun check(item: GlobalRateCardBaseline): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class GlobalRateCardBaselineAllowedLabelRule(
    var allowed: MutableList<String>,
) : GlobalRateCardBaselineRule {
    override fun check(item: GlobalRateCardBaseline): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.label))) {
            return mutableListOf<String>("label ${item.label} is expired")
        }
        return mutableListOf<String>()
    }
}

data class GlobalRateCardBaselineRequiredLabelRule(var message: String) : GlobalRateCardBaselineRule {
    override fun check(item: GlobalRateCardBaseline): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a global rate card baseline. */
data class GlobalRateCardBaselineValidator(var rules: MutableList<GlobalRateCardBaselineRule>) {
    fun validate(item: GlobalRateCardBaseline): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: GlobalRateCardBaseline) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<GlobalRateCardBaseline>): MutableMap<String, MutableList<String>> {
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

fun defaultGlobalRateCardBaselineValidator(): GlobalRateCardBaselineValidator {
    return GlobalRateCardBaselineValidator(
        mutableListOf<GlobalRateCardBaselineRule>(
            GlobalRateCardBaselineCodeLengthRule(32),
            GlobalRateCardBaselineAllowedLabelRule(mutableListOf<String>("lenient", "fallback", "pending")),
            GlobalRateCardBaselineRequiredLabelRule("global rate card baseline needs a value"),
        ),
    )
}
