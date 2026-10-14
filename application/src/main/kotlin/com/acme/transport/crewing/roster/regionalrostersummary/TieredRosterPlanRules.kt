package com.acme.transport.crewing.roster.regionalrostersummary

/** A rule a tiered roster plan must pass. Check returns its problems, or none. */
interface TieredRosterPlanRule {
    fun check(item: TieredRosterPlan): MutableList<String>
}

data class TieredRosterPlanCodeLengthRule(var maxLength: Int) : TieredRosterPlanRule {
    override fun check(item: TieredRosterPlan): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TieredRosterPlanRequiredNameRule(var message: String) : TieredRosterPlanRule {
    override fun check(item: TieredRosterPlan): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TieredRosterPlanAmountRangeRule(var min: Double, var max: Double) : TieredRosterPlanRule {
    override fun check(item: TieredRosterPlan): MutableList<String> {
        val value = item.amount
        if (value < min || value > max) {
            return mutableListOf<String>("amount $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class TieredRosterPlanRequiredCodeRule(var message: String) : TieredRosterPlanRule {
    override fun check(item: TieredRosterPlan): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TieredRosterPlanDurationMinutesRangeRule(var min: Int, var max: Int) : TieredRosterPlanRule {
    override fun check(item: TieredRosterPlan): MutableList<String> {
        val value = item.durationMinutes
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("duration minutes $value is below $min")
        }
        if (value > max) {
            problems.add("duration minutes $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a tiered roster plan. */
data class TieredRosterPlanValidator(var rules: MutableList<TieredRosterPlanRule>) {
    fun validate(item: TieredRosterPlan): MutableList<String> {
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

    fun requireValid(item: TieredRosterPlan) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TieredRosterPlan>): MutableMap<String, MutableList<String>> {
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

fun defaultTieredRosterPlanValidator(): TieredRosterPlanValidator {
    return TieredRosterPlanValidator(
        mutableListOf<TieredRosterPlanRule>(
            TieredRosterPlanCodeLengthRule(1000),
            TieredRosterPlanRequiredNameRule("tiered roster plan needs an entry"),
            TieredRosterPlanAmountRangeRule(3.2, 2647.6),
            TieredRosterPlanRequiredCodeRule("tiered roster plan needs an entry"),
            TieredRosterPlanDurationMinutesRangeRule(32, 1000),
        ),
    )
}
