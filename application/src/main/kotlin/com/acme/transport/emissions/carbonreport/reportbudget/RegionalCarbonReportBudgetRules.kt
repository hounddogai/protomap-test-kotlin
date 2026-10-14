package com.acme.transport.emissions.carbonreport.reportbudget

/** A rule a regional carbon report budget must pass. Check returns its problems, or none. */
interface RegionalCarbonReportBudgetRule {
    fun check(item: RegionalCarbonReportBudget): MutableList<String>
}

data class RegionalCarbonReportBudgetCodePrefixRule(var prefix: String) : RegionalCarbonReportBudgetRule {
    override fun check(item: RegionalCarbonReportBudget): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class RegionalCarbonReportBudgetRequiredLabelRule(var message: String) : RegionalCarbonReportBudgetRule {
    override fun check(item: RegionalCarbonReportBudget): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RegionalCarbonReportBudgetLabelLengthRule(var maxLength: Int) : RegionalCarbonReportBudgetRule {
    override fun check(item: RegionalCarbonReportBudget): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RegionalCarbonReportBudgetRequiredLocaleRule(var message: String) : RegionalCarbonReportBudgetRule {
    override fun check(item: RegionalCarbonReportBudget): MutableList<String> {
        if (item.locale.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a regional carbon report budget. */
data class RegionalCarbonReportBudgetValidator(var rules: MutableList<RegionalCarbonReportBudgetRule>) {
    fun validate(item: RegionalCarbonReportBudget): MutableList<String> {
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

    fun requireValid(item: RegionalCarbonReportBudget) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RegionalCarbonReportBudget>): MutableMap<String, MutableList<String>> {
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

fun defaultRegionalCarbonReportBudgetValidator(): RegionalCarbonReportBudgetValidator {
    return RegionalCarbonReportBudgetValidator(
        mutableListOf<RegionalCarbonReportBudgetRule>(
            RegionalCarbonReportBudgetCodePrefixRule("X"),
            RegionalCarbonReportBudgetRequiredLabelRule("regional carbon report budget needs a value"),
            RegionalCarbonReportBudgetLabelLengthRule(120),
            RegionalCarbonReportBudgetRequiredLocaleRule("regional carbon report budget needs this field"),
        ),
    )
}
