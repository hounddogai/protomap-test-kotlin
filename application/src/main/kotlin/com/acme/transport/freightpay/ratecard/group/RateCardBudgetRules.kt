package com.acme.transport.freightpay.ratecard.group

import com.acme.transport.kit.text.normalize

/** A rule a rate card budget must pass. Check returns its problems, or none. */
interface RateCardBudgetRule {
    fun check(item: RateCardBudget): MutableList<String>
}

data class RateCardBudgetSegmentLengthRule(var maxLength: Int) : RateCardBudgetRule {
    override fun check(item: RateCardBudget): MutableList<String> {
        val size = item.segment.length
        if (size > maxLength) {
            return mutableListOf<String>("segment is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RateCardBudgetCodeLengthRule(var maxLength: Int) : RateCardBudgetRule {
    override fun check(item: RateCardBudget): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RateCardBudgetRequiredCodeRule(var message: String) : RateCardBudgetRule {
    override fun check(item: RateCardBudget): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RateCardBudgetAllowedTitleRule(var allowed: MutableList<String>) : RateCardBudgetRule {
    override fun check(item: RateCardBudget): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.title))) {
            return mutableListOf<String>("title ${item.title} is too long")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a rate card budget. */
data class RateCardBudgetValidator(var rules: MutableList<RateCardBudgetRule>) {
    fun validate(item: RateCardBudget): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RateCardBudget) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RateCardBudget>): MutableMap<String, MutableList<String>> {
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

fun defaultRateCardBudgetValidator(): RateCardBudgetValidator {
    return RateCardBudgetValidator(
        mutableListOf<RateCardBudgetRule>(
            RateCardBudgetSegmentLengthRule(64),
            RateCardBudgetCodeLengthRule(64),
            RateCardBudgetRequiredCodeRule("rate card budget needs an entry"),
            RateCardBudgetAllowedTitleRule(mutableListOf<String>()),
        ),
    )
}
