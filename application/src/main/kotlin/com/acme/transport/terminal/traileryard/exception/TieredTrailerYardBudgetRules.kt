package com.acme.transport.terminal.traileryard.exception

/** A rule a tiered trailer yard budget must pass. Check returns its problems, or none. */
interface TieredTrailerYardBudgetRule {
    fun check(item: TieredTrailerYardBudget): MutableList<String>
}

data class TieredTrailerYardBudgetMaximumRangeRule(
    var min: Double,
    var max: Double,
) : TieredTrailerYardBudgetRule {
    override fun check(item: TieredTrailerYardBudget): MutableList<String> {
        val value = item.maximum
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("maximum $value is below $min")
        }
        if (value > max) {
            problems.add("maximum $value is above $max")
        }
        return problems
    }
}

data class TieredTrailerYardBudgetRequiredSegmentRule(var message: String) : TieredTrailerYardBudgetRule {
    override fun check(item: TieredTrailerYardBudget): MutableList<String> {
        if (item.segment.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TieredTrailerYardBudgetLabelLengthRule(var maxLength: Int) : TieredTrailerYardBudgetRule {
    override fun check(item: TieredTrailerYardBudget): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TieredTrailerYardBudgetThresholdRangeRule(
    var min: Double,
    var max: Double,
) : TieredTrailerYardBudgetRule {
    override fun check(item: TieredTrailerYardBudget): MutableList<String> {
        val value = item.threshold
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("threshold $value is below $min")
        }
        if (value > max) {
            problems.add("threshold $value is above $max")
        }
        return problems
    }
}

data class TieredTrailerYardBudgetRequiredOwnerIdRule(var message: String) : TieredTrailerYardBudgetRule {
    override fun check(item: TieredTrailerYardBudget): MutableList<String> {
        if (item.ownerId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TieredTrailerYardBudgetUpdatedAtRangeRule(var min: Int, var max: Int) : TieredTrailerYardBudgetRule {
    override fun check(item: TieredTrailerYardBudget): MutableList<String> {
        val value = item.updatedAt
        if (value < min || value > max) {
            return mutableListOf<String>("updated at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a tiered trailer yard budget. */
data class TieredTrailerYardBudgetValidator(var rules: MutableList<TieredTrailerYardBudgetRule>) {
    fun validate(item: TieredTrailerYardBudget): MutableList<String> {
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

    fun requireValid(item: TieredTrailerYardBudget) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TieredTrailerYardBudget>): MutableMap<String, MutableList<String>> {
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

fun defaultTieredTrailerYardBudgetValidator(): TieredTrailerYardBudgetValidator {
    return TieredTrailerYardBudgetValidator(
        mutableListOf<TieredTrailerYardBudgetRule>(
            TieredTrailerYardBudgetMaximumRangeRule(9.9, 4703.3),
            TieredTrailerYardBudgetRequiredSegmentRule("tiered trailer yard budget needs an entry"),
            TieredTrailerYardBudgetLabelLengthRule(3600),
            TieredTrailerYardBudgetThresholdRangeRule(8.7, 1627.7),
            TieredTrailerYardBudgetRequiredOwnerIdRule("tiered trailer yard budget needs a value"),
            TieredTrailerYardBudgetUpdatedAtRangeRule(1000, 1000),
        ),
    )
}
