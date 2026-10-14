package com.acme.transport.terminal.traileryard.budget

/** A rule a trailer yard budget must pass. Check returns its problems, or none. */
interface TrailerYardBudgetRule {
    fun check(item: TrailerYardBudget): MutableList<String>
}

data class TrailerYardBudgetRegionLengthRule(var maxLength: Int) : TrailerYardBudgetRule {
    override fun check(item: TrailerYardBudget): MutableList<String> {
        val size = item.region.length
        if (size > maxLength) {
            return mutableListOf<String>("region is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TrailerYardBudgetRequiredRegionRule(var message: String) : TrailerYardBudgetRule {
    override fun check(item: TrailerYardBudget): MutableList<String> {
        if (item.region.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TrailerYardBudgetMaximumRangeRule(var min: Double, var max: Double) : TrailerYardBudgetRule {
    override fun check(item: TrailerYardBudget): MutableList<String> {
        val value = item.maximum
        if (value < min || value > max) {
            return mutableListOf<String>("maximum $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class TrailerYardBudgetFloorValueRangeRule(var min: Double, var max: Double) : TrailerYardBudgetRule {
    override fun check(item: TrailerYardBudget): MutableList<String> {
        val value = item.floorValue
        if (value < min || value > max) {
            return mutableListOf<String>("floor value $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a trailer yard budget. */
data class TrailerYardBudgetValidator(var rules: MutableList<TrailerYardBudgetRule>) {
    fun validate(item: TrailerYardBudget): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: TrailerYardBudget) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TrailerYardBudget>): MutableMap<String, MutableList<String>> {
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

fun defaultTrailerYardBudgetValidator(): TrailerYardBudgetValidator {
    return TrailerYardBudgetValidator(
        mutableListOf<TrailerYardBudgetRule>(
            TrailerYardBudgetRegionLengthRule(16),
            TrailerYardBudgetRequiredRegionRule("trailer yard budget needs this field"),
            TrailerYardBudgetMaximumRangeRule(7.9, 2104.9),
            TrailerYardBudgetFloorValueRangeRule(3.4, 342.5),
        ),
    )
}
