package com.acme.transport.customs.clearance.quota

/** A rule a adaptive clearance budget must pass. Check returns its problems, or none. */
interface AdaptiveClearanceBudgetRule {
    fun check(item: AdaptiveClearanceBudget): MutableList<String>
}

data class AdaptiveClearanceBudgetRequiredLabelRule(var message: String) : AdaptiveClearanceBudgetRule {
    override fun check(item: AdaptiveClearanceBudget): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveClearanceBudgetRequiredCodeRule(var message: String) : AdaptiveClearanceBudgetRule {
    override fun check(item: AdaptiveClearanceBudget): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveClearanceBudgetEndsAtRangeRule(var min: Int, var max: Int) : AdaptiveClearanceBudgetRule {
    override fun check(item: AdaptiveClearanceBudget): MutableList<String> {
        val value = item.endsAt
        if (value < min || value > max) {
            return mutableListOf<String>("ends at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveClearanceBudgetCodeLengthRule(var maxLength: Int) : AdaptiveClearanceBudgetRule {
    override fun check(item: AdaptiveClearanceBudget): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveClearanceBudgetRequiredLaneRule(var message: String) : AdaptiveClearanceBudgetRule {
    override fun check(item: AdaptiveClearanceBudget): MutableList<String> {
        if (item.lane.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a adaptive clearance budget. */
data class AdaptiveClearanceBudgetValidator(var rules: MutableList<AdaptiveClearanceBudgetRule>) {
    fun validate(item: AdaptiveClearanceBudget): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: AdaptiveClearanceBudget) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AdaptiveClearanceBudget>): MutableMap<String, MutableList<String>> {
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

fun defaultAdaptiveClearanceBudgetValidator(): AdaptiveClearanceBudgetValidator {
    return AdaptiveClearanceBudgetValidator(
        mutableListOf<AdaptiveClearanceBudgetRule>(
            AdaptiveClearanceBudgetRequiredLabelRule("adaptive clearance budget needs an entry"),
            AdaptiveClearanceBudgetRequiredCodeRule("adaptive clearance budget needs an entry"),
            AdaptiveClearanceBudgetEndsAtRangeRule(32, 1000),
            AdaptiveClearanceBudgetCodeLengthRule(32),
            AdaptiveClearanceBudgetRequiredLaneRule("adaptive clearance budget needs this field"),
        ),
    )
}
