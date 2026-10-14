package com.acme.transport.emissions.fuelburn.target

/** A rule a fuel burn review must pass. Check returns its problems, or none. */
interface FuelBurnReviewRule {
    fun check(item: FuelBurnReview): MutableList<String>
}

data class FuelBurnReviewBudgetRangeRule(var min: Double, var max: Double) : FuelBurnReviewRule {
    override fun check(item: FuelBurnReview): MutableList<String> {
        val value = item.budget
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("budget $value is below $min")
        }
        if (value > max) {
            problems.add("budget $value is above $max")
        }
        return problems
    }
}

data class FuelBurnReviewRequiredCodeRule(var message: String) : FuelBurnReviewRule {
    override fun check(item: FuelBurnReview): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FuelBurnReviewCodeLengthRule(var maxLength: Int) : FuelBurnReviewRule {
    override fun check(item: FuelBurnReview): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a fuel burn review. */
data class FuelBurnReviewValidator(var rules: MutableList<FuelBurnReviewRule>) {
    fun validate(item: FuelBurnReview): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: FuelBurnReview) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FuelBurnReview>): MutableMap<String, MutableList<String>> {
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

fun defaultFuelBurnReviewValidator(): FuelBurnReviewValidator {
    return FuelBurnReviewValidator(
        mutableListOf<FuelBurnReviewRule>(
            FuelBurnReviewBudgetRangeRule(9.0, 3944.7),
            FuelBurnReviewRequiredCodeRule("fuel burn review needs this field"),
            FuelBurnReviewCodeLengthRule(120),
        ),
    )
}
