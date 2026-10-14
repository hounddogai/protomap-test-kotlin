package com.acme.transport.freightpay.ratecard.forecast

/** A rule a rate card plan must pass. Check returns its problems, or none. */
interface RateCardPlanRule {
    fun check(item: RateCardPlan): MutableList<String>
}

data class RateCardPlanCostRangeRule(var min: Double, var max: Double) : RateCardPlanRule {
    override fun check(item: RateCardPlan): MutableList<String> {
        val value = item.cost
        if (value < min || value > max) {
            return mutableListOf<String>("cost $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class RateCardPlanCodePrefixRule(var prefix: String) : RateCardPlanRule {
    override fun check(item: RateCardPlan): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class RateCardPlanRequiredUnitRule(var message: String) : RateCardPlanRule {
    override fun check(item: RateCardPlan): MutableList<String> {
        if (item.unit.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RateCardPlanRequiredDepotRule(var message: String) : RateCardPlanRule {
    override fun check(item: RateCardPlan): MutableList<String> {
        if (item.depot.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a rate card plan. */
data class RateCardPlanValidator(var rules: MutableList<RateCardPlanRule>) {
    fun validate(item: RateCardPlan): MutableList<String> {
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

    fun requireValid(item: RateCardPlan) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RateCardPlan>): MutableMap<String, MutableList<String>> {
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

fun defaultRateCardPlanValidator(): RateCardPlanValidator {
    return RateCardPlanValidator(
        mutableListOf<RateCardPlanRule>(
            RateCardPlanCostRangeRule(4.6, 4934.8),
            RateCardPlanCodePrefixRule("OPS"),
            RateCardPlanRequiredUnitRule("rate card plan needs a value"),
            RateCardPlanRequiredDepotRule("rate card plan needs an entry"),
        ),
    )
}
