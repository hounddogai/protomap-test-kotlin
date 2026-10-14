package com.acme.transport.freightpay.freightinvoice.rollup

import com.acme.transport.kit.text.normalize

/** A rule a freight invoice rollup must pass. Check returns its problems, or none. */
interface FreightInvoiceRollupRule {
    fun check(item: FreightInvoiceRollup): MutableList<String>
}

data class FreightInvoiceRollupRequiredSourceRule(var message: String) : FreightInvoiceRollupRule {
    override fun check(item: FreightInvoiceRollup): MutableList<String> {
        if (item.source.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FreightInvoiceRollupAllowedTitleRule(var allowed: MutableList<String>) : FreightInvoiceRollupRule {
    override fun check(item: FreightInvoiceRollup): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.title))) {
            return mutableListOf<String>("title ${item.title} is above maximum")
        }
        return mutableListOf<String>()
    }
}

data class FreightInvoiceRollupScoreRangeRule(var min: Double, var max: Double) : FreightInvoiceRollupRule {
    override fun check(item: FreightInvoiceRollup): MutableList<String> {
        val value = item.score
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("score $value is below $min")
        }
        if (value > max) {
            problems.add("score $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a freight invoice rollup. */
data class FreightInvoiceRollupValidator(var rules: MutableList<FreightInvoiceRollupRule>) {
    fun validate(item: FreightInvoiceRollup): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: FreightInvoiceRollup) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FreightInvoiceRollup>): MutableMap<String, MutableList<String>> {
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

fun defaultFreightInvoiceRollupValidator(): FreightInvoiceRollupValidator {
    return FreightInvoiceRollupValidator(
        mutableListOf<FreightInvoiceRollupRule>(
            FreightInvoiceRollupRequiredSourceRule("freight invoice rollup needs this field"),
            FreightInvoiceRollupAllowedTitleRule(mutableListOf<String>()),
            FreightInvoiceRollupScoreRangeRule(2.9, 2506.0),
        ),
    )
}
