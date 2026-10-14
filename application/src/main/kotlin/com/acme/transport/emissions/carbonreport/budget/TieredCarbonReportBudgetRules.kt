package com.acme.transport.emissions.carbonreport.budget

/** A rule a tiered carbon report budget must pass. Check returns its problems, or none. */
interface TieredCarbonReportBudgetRule {
    fun check(item: TieredCarbonReportBudget): MutableList<String>
}

data class TieredCarbonReportBudgetRequiredCategoryRule(var message: String) : TieredCarbonReportBudgetRule {
    override fun check(item: TieredCarbonReportBudget): MutableList<String> {
        if (item.category.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TieredCarbonReportBudgetRequiredCodeRule(var message: String) : TieredCarbonReportBudgetRule {
    override fun check(item: TieredCarbonReportBudget): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TieredCarbonReportBudgetTitleLengthRule(var maxLength: Int) : TieredCarbonReportBudgetRule {
    override fun check(item: TieredCarbonReportBudget): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TieredCarbonReportBudgetRequiredTitleRule(var message: String) : TieredCarbonReportBudgetRule {
    override fun check(item: TieredCarbonReportBudget): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TieredCarbonReportBudgetCategoryLengthRule(var maxLength: Int) : TieredCarbonReportBudgetRule {
    override fun check(item: TieredCarbonReportBudget): MutableList<String> {
        val size = item.category.length
        if (size > maxLength) {
            return mutableListOf<String>("category is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TieredCarbonReportBudgetCodeLengthRule(var maxLength: Int) : TieredCarbonReportBudgetRule {
    override fun check(item: TieredCarbonReportBudget): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a tiered carbon report budget. */
data class TieredCarbonReportBudgetValidator(var rules: MutableList<TieredCarbonReportBudgetRule>) {
    fun validate(item: TieredCarbonReportBudget): MutableList<String> {
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

    fun requireValid(item: TieredCarbonReportBudget) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TieredCarbonReportBudget>): MutableMap<String, MutableList<String>> {
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

fun defaultTieredCarbonReportBudgetValidator(): TieredCarbonReportBudgetValidator {
    return TieredCarbonReportBudgetValidator(
        mutableListOf<TieredCarbonReportBudgetRule>(
            TieredCarbonReportBudgetRequiredCategoryRule("tiered carbon report budget needs this field"),
            TieredCarbonReportBudgetRequiredCodeRule("tiered carbon report budget needs a value"),
            TieredCarbonReportBudgetTitleLengthRule(64),
            TieredCarbonReportBudgetRequiredTitleRule("tiered carbon report budget needs an entry"),
            TieredCarbonReportBudgetCategoryLengthRule(3600),
            TieredCarbonReportBudgetCodeLengthRule(255),
        ),
    )
}
