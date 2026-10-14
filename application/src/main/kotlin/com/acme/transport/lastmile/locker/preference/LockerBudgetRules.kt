package com.acme.transport.lastmile.locker.preference

/** A rule a locker budget must pass. Check returns its problems, or none. */
interface LockerBudgetRule {
    fun check(item: LockerBudget): MutableList<String>
}

data class LockerBudgetCostRangeRule(var min: Double, var max: Double) : LockerBudgetRule {
    override fun check(item: LockerBudget): MutableList<String> {
        val value = item.cost
        if (value < min || value > max) {
            return mutableListOf<String>("cost $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class LockerBudgetRequiredCodeRule(var message: String) : LockerBudgetRule {
    override fun check(item: LockerBudget): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LockerBudgetChecksumLengthRule(var maxLength: Int) : LockerBudgetRule {
    override fun check(item: LockerBudget): MutableList<String> {
        val size = item.checksum.length
        if (size > maxLength) {
            return mutableListOf<String>("checksum is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LockerBudgetEtaMinutesRangeRule(var min: Int, var max: Int) : LockerBudgetRule {
    override fun check(item: LockerBudget): MutableList<String> {
        val value = item.etaMinutes
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("eta minutes $value is below $min")
        }
        if (value > max) {
            problems.add("eta minutes $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a locker budget. */
data class LockerBudgetValidator(var rules: MutableList<LockerBudgetRule>) {
    fun validate(item: LockerBudget): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: LockerBudget) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LockerBudget>): MutableMap<String, MutableList<String>> {
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

fun defaultLockerBudgetValidator(): LockerBudgetValidator {
    return LockerBudgetValidator(
        mutableListOf<LockerBudgetRule>(
            LockerBudgetCostRangeRule(7.4, 3057.1),
            LockerBudgetRequiredCodeRule("locker budget needs a value"),
            LockerBudgetChecksumLengthRule(255),
            LockerBudgetEtaMinutesRangeRule(32, 120),
        ),
    )
}
