package com.acme.transport.freightpay.ratecard.cardbundle

/** A rule a strict rate card plan must pass. Check returns its problems, or none. */
interface StrictRateCardPlanRule {
    fun check(item: StrictRateCardPlan): MutableList<String>
}

data class StrictRateCardPlanRequiredDescriptionRule(var message: String) : StrictRateCardPlanRule {
    override fun check(item: StrictRateCardPlan): MutableList<String> {
        if (item.description.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class StrictRateCardPlanPalletsRangeRule(var min: Int, var max: Int) : StrictRateCardPlanRule {
    override fun check(item: StrictRateCardPlan): MutableList<String> {
        val value = item.pallets
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("pallets $value is below $min")
        }
        if (value > max) {
            problems.add("pallets $value is above $max")
        }
        return problems
    }
}

data class StrictRateCardPlanCodeLengthRule(var maxLength: Int) : StrictRateCardPlanRule {
    override fun check(item: StrictRateCardPlan): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a strict rate card plan. */
data class StrictRateCardPlanValidator(var rules: MutableList<StrictRateCardPlanRule>) {
    fun validate(item: StrictRateCardPlan): MutableList<String> {
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

    fun requireValid(item: StrictRateCardPlan) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<StrictRateCardPlan>): MutableMap<String, MutableList<String>> {
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

fun defaultStrictRateCardPlanValidator(): StrictRateCardPlanValidator {
    return StrictRateCardPlanValidator(
        mutableListOf<StrictRateCardPlanRule>(
            StrictRateCardPlanRequiredDescriptionRule("strict rate card plan needs an entry"),
            StrictRateCardPlanPalletsRangeRule(64, 1000),
            StrictRateCardPlanCodeLengthRule(32),
        ),
    )
}
