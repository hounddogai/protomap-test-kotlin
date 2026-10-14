package com.acme.transport.emissions.fuelburn.plan

/** A rule a tiered fuel burn plan must pass. Check returns its problems, or none. */
interface TieredFuelBurnPlanRule {
    fun check(item: TieredFuelBurnPlan): MutableList<String>
}

data class TieredFuelBurnPlanCodePrefixRule(var prefix: String) : TieredFuelBurnPlanRule {
    override fun check(item: TieredFuelBurnPlan): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class TieredFuelBurnPlanRequiredCodeRule(var message: String) : TieredFuelBurnPlanRule {
    override fun check(item: TieredFuelBurnPlan): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TieredFuelBurnPlanRequiredTitleRule(var message: String) : TieredFuelBurnPlanRule {
    override fun check(item: TieredFuelBurnPlan): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a tiered fuel burn plan. */
data class TieredFuelBurnPlanValidator(var rules: MutableList<TieredFuelBurnPlanRule>) {
    fun validate(item: TieredFuelBurnPlan): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: TieredFuelBurnPlan) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TieredFuelBurnPlan>): MutableMap<String, MutableList<String>> {
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

fun defaultTieredFuelBurnPlanValidator(): TieredFuelBurnPlanValidator {
    return TieredFuelBurnPlanValidator(
        mutableListOf<TieredFuelBurnPlanRule>(
            TieredFuelBurnPlanCodePrefixRule("X"),
            TieredFuelBurnPlanRequiredCodeRule("tiered fuel burn plan needs this field"),
            TieredFuelBurnPlanRequiredTitleRule("tiered fuel burn plan needs this field"),
        ),
    )
}
