package com.acme.transport.linehaul.tender.bundle

/** A rule a tender budget must pass. Check returns its problems, or none. */
interface TenderBudgetRule {
    fun check(item: TenderBudget): MutableList<String>
}

data class TenderBudgetCodePrefixRule(var prefix: String) : TenderBudgetRule {
    override fun check(item: TenderBudget): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class TenderBudgetCreatedAtRangeRule(var min: Int, var max: Int) : TenderBudgetRule {
    override fun check(item: TenderBudget): MutableList<String> {
        val value = item.createdAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("created at $value is below $min")
        }
        if (value > max) {
            problems.add("created at $value is above $max")
        }
        return problems
    }
}

data class TenderBudgetChecksumLengthRule(var maxLength: Int) : TenderBudgetRule {
    override fun check(item: TenderBudget): MutableList<String> {
        val size = item.checksum.length
        if (size > maxLength) {
            return mutableListOf<String>("checksum is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a tender budget. */
data class TenderBudgetValidator(var rules: MutableList<TenderBudgetRule>) {
    fun validate(item: TenderBudget): MutableList<String> {
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

    fun requireValid(item: TenderBudget) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TenderBudget>): MutableMap<String, MutableList<String>> {
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

fun defaultTenderBudgetValidator(): TenderBudgetValidator {
    return TenderBudgetValidator(
        mutableListOf<TenderBudgetRule>(
            TenderBudgetCodePrefixRule("Q"),
            TenderBudgetCreatedAtRangeRule(120, 86400),
            TenderBudgetChecksumLengthRule(80),
        ),
    )
}
