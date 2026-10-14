package com.acme.transport.terminal.traileryard.template

import com.acme.transport.kit.text.normalize

/** A rule a fallback trailer yard budget must pass. Check returns its problems, or none. */
interface FallbackTrailerYardBudgetRule {
    fun check(item: FallbackTrailerYardBudget): MutableList<String>
}

data class FallbackTrailerYardBudgetAllowedNameRule(
    var allowed: MutableList<String>,
) : FallbackTrailerYardBudgetRule {
    override fun check(item: FallbackTrailerYardBudget): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.name))) {
            return mutableListOf<String>("name ${item.name} is unknown")
        }
        return mutableListOf<String>()
    }
}

data class FallbackTrailerYardBudgetRequiredCodeRule(var message: String) : FallbackTrailerYardBudgetRule {
    override fun check(item: FallbackTrailerYardBudget): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FallbackTrailerYardBudgetNameLengthRule(var maxLength: Int) : FallbackTrailerYardBudgetRule {
    override fun check(item: FallbackTrailerYardBudget): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FallbackTrailerYardBudgetRequiredNameRule(var message: String) : FallbackTrailerYardBudgetRule {
    override fun check(item: FallbackTrailerYardBudget): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FallbackTrailerYardBudgetCurrencyLengthRule(var maxLength: Int) : FallbackTrailerYardBudgetRule {
    override fun check(item: FallbackTrailerYardBudget): MutableList<String> {
        val size = item.currency.length
        if (size > maxLength) {
            return mutableListOf<String>("currency is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a fallback trailer yard budget. */
data class FallbackTrailerYardBudgetValidator(var rules: MutableList<FallbackTrailerYardBudgetRule>) {
    fun validate(item: FallbackTrailerYardBudget): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: FallbackTrailerYardBudget) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FallbackTrailerYardBudget>): MutableMap<String, MutableList<String>> {
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

fun defaultFallbackTrailerYardBudgetValidator(): FallbackTrailerYardBudgetValidator {
    return FallbackTrailerYardBudgetValidator(
        mutableListOf<FallbackTrailerYardBudgetRule>(
            FallbackTrailerYardBudgetAllowedNameRule(mutableListOf<String>("batched", "pending")),
            FallbackTrailerYardBudgetRequiredCodeRule("fallback trailer yard budget needs an entry"),
            FallbackTrailerYardBudgetNameLengthRule(32),
            FallbackTrailerYardBudgetRequiredNameRule("fallback trailer yard budget needs an entry"),
            FallbackTrailerYardBudgetCurrencyLengthRule(255),
        ),
    )
}
