package com.acme.transport.emissions.carbonreport.reportadjustment

/** A rule a pending carbon report budget must pass. Check returns its problems, or none. */
interface PendingCarbonReportBudgetRule {
    fun check(item: PendingCarbonReportBudget): MutableList<String>
}

data class PendingCarbonReportBudgetApprovedGuardRule(var limit: Int) : PendingCarbonReportBudgetRule {
    override fun check(item: PendingCarbonReportBudget): MutableList<String> {
        if (item.approved && item.sequence > limit) {
            return mutableListOf<String>("sequence ${item.sequence} exceeds $limit while approved")
        }
        return mutableListOf<String>()
    }
}

data class PendingCarbonReportBudgetTitleLengthRule(var maxLength: Int) : PendingCarbonReportBudgetRule {
    override fun check(item: PendingCarbonReportBudget): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PendingCarbonReportBudgetRequiredTitleRule(var message: String) : PendingCarbonReportBudgetRule {
    override fun check(item: PendingCarbonReportBudget): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PendingCarbonReportBudgetRequiredLaneRule(var message: String) : PendingCarbonReportBudgetRule {
    override fun check(item: PendingCarbonReportBudget): MutableList<String> {
        if (item.lane.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a pending carbon report budget. */
data class PendingCarbonReportBudgetValidator(var rules: MutableList<PendingCarbonReportBudgetRule>) {
    fun validate(item: PendingCarbonReportBudget): MutableList<String> {
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

    fun requireValid(item: PendingCarbonReportBudget) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PendingCarbonReportBudget>): MutableMap<String, MutableList<String>> {
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

fun defaultPendingCarbonReportBudgetValidator(): PendingCarbonReportBudgetValidator {
    return PendingCarbonReportBudgetValidator(
        mutableListOf<PendingCarbonReportBudgetRule>(
            PendingCarbonReportBudgetApprovedGuardRule(80),
            PendingCarbonReportBudgetTitleLengthRule(80),
            PendingCarbonReportBudgetRequiredTitleRule("pending carbon report budget needs this field"),
            PendingCarbonReportBudgetRequiredLaneRule("pending carbon report budget needs a value"),
        ),
    )
}
