package com.acme.transport.emissions.fuelburn.schedule

/** A rule a fuel burn plan must pass. Check returns its problems, or none. */
interface FuelBurnPlanRule {
    fun check(item: FuelBurnPlan): MutableList<String>
}

data class FuelBurnPlanCodeLengthRule(var maxLength: Int) : FuelBurnPlanRule {
    override fun check(item: FuelBurnPlan): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FuelBurnPlanLocaleLengthRule(var maxLength: Int) : FuelBurnPlanRule {
    override fun check(item: FuelBurnPlan): MutableList<String> {
        val size = item.locale.length
        if (size > maxLength) {
            return mutableListOf<String>("locale is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FuelBurnPlanRetriesRangeRule(var min: Int, var max: Int) : FuelBurnPlanRule {
    override fun check(item: FuelBurnPlan): MutableList<String> {
        val value = item.retries
        if (value < min || value > max) {
            return mutableListOf<String>("retries $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class FuelBurnPlanCodePrefixRule(var prefix: String) : FuelBurnPlanRule {
    override fun check(item: FuelBurnPlan): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class FuelBurnPlanReviewerLengthRule(var maxLength: Int) : FuelBurnPlanRule {
    override fun check(item: FuelBurnPlan): MutableList<String> {
        val size = item.reviewer.length
        if (size > maxLength) {
            return mutableListOf<String>("reviewer is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a fuel burn plan. */
data class FuelBurnPlanValidator(var rules: MutableList<FuelBurnPlanRule>) {
    fun validate(item: FuelBurnPlan): MutableList<String> {
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

    fun requireValid(item: FuelBurnPlan) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FuelBurnPlan>): MutableMap<String, MutableList<String>> {
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

fun defaultFuelBurnPlanValidator(): FuelBurnPlanValidator {
    return FuelBurnPlanValidator(
        mutableListOf<FuelBurnPlanRule>(
            FuelBurnPlanCodeLengthRule(1000),
            FuelBurnPlanLocaleLengthRule(86400),
            FuelBurnPlanRetriesRangeRule(16, 3600),
            FuelBurnPlanCodePrefixRule("ACM"),
            FuelBurnPlanReviewerLengthRule(1000),
        ),
    )
}
