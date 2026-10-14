package com.acme.transport.safety.coaching.coachingmetric

/** A rule a pending coaching budget must pass. Check returns its problems, or none. */
interface PendingCoachingBudgetRule {
    fun check(item: PendingCoachingBudget): MutableList<String>
}

data class PendingCoachingBudgetRequiredLabelRule(var message: String) : PendingCoachingBudgetRule {
    override fun check(item: PendingCoachingBudget): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PendingCoachingBudgetSegmentLengthRule(var maxLength: Int) : PendingCoachingBudgetRule {
    override fun check(item: PendingCoachingBudget): MutableList<String> {
        val size = item.segment.length
        if (size > maxLength) {
            return mutableListOf<String>("segment is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PendingCoachingBudgetRequiredDriverIdRule(var message: String) : PendingCoachingBudgetRule {
    override fun check(item: PendingCoachingBudget): MutableList<String> {
        if (item.driverId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PendingCoachingBudgetCodeLengthRule(var maxLength: Int) : PendingCoachingBudgetRule {
    override fun check(item: PendingCoachingBudget): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PendingCoachingBudgetDriverIdLengthRule(var maxLength: Int) : PendingCoachingBudgetRule {
    override fun check(item: PendingCoachingBudget): MutableList<String> {
        val size = item.driverId.length
        if (size > maxLength) {
            return mutableListOf<String>("driver id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a pending coaching budget. */
data class PendingCoachingBudgetValidator(var rules: MutableList<PendingCoachingBudgetRule>) {
    fun validate(item: PendingCoachingBudget): MutableList<String> {
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

    fun requireValid(item: PendingCoachingBudget) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PendingCoachingBudget>): MutableMap<String, MutableList<String>> {
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

fun defaultPendingCoachingBudgetValidator(): PendingCoachingBudgetValidator {
    return PendingCoachingBudgetValidator(
        mutableListOf<PendingCoachingBudgetRule>(
            PendingCoachingBudgetRequiredLabelRule("pending coaching budget needs a value"),
            PendingCoachingBudgetSegmentLengthRule(86400),
            PendingCoachingBudgetRequiredDriverIdRule("pending coaching budget needs this field"),
            PendingCoachingBudgetCodeLengthRule(32),
            PendingCoachingBudgetDriverIdLengthRule(1000),
        ),
    )
}
