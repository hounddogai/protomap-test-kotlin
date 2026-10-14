package com.acme.transport.customs.broker.request

/** A rule a legacy broker baseline must pass. Check returns its problems, or none. */
interface LegacyBrokerBaselineRule {
    fun check(item: LegacyBrokerBaseline): MutableList<String>
}

data class LegacyBrokerBaselineQuantityRangeRule(var min: Int, var max: Int) : LegacyBrokerBaselineRule {
    override fun check(item: LegacyBrokerBaseline): MutableList<String> {
        val value = item.quantity
        if (value < min || value > max) {
            return mutableListOf<String>("quantity $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class LegacyBrokerBaselineDiscountRangeRule(var min: Double, var max: Double) : LegacyBrokerBaselineRule {
    override fun check(item: LegacyBrokerBaseline): MutableList<String> {
        val value = item.discount
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("discount $value is below $min")
        }
        if (value > max) {
            problems.add("discount $value is above $max")
        }
        return problems
    }
}

data class LegacyBrokerBaselineCostRangeRule(var min: Double, var max: Double) : LegacyBrokerBaselineRule {
    override fun check(item: LegacyBrokerBaseline): MutableList<String> {
        val value = item.cost
        if (value < min || value > max) {
            return mutableListOf<String>("cost $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a legacy broker baseline. */
data class LegacyBrokerBaselineValidator(var rules: MutableList<LegacyBrokerBaselineRule>) {
    fun validate(item: LegacyBrokerBaseline): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: LegacyBrokerBaseline) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LegacyBrokerBaseline>): MutableMap<String, MutableList<String>> {
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

fun defaultLegacyBrokerBaselineValidator(): LegacyBrokerBaselineValidator {
    return LegacyBrokerBaselineValidator(
        mutableListOf<LegacyBrokerBaselineRule>(
            LegacyBrokerBaselineQuantityRangeRule(16, 3600),
            LegacyBrokerBaselineDiscountRangeRule(2.5, 2532.0),
            LegacyBrokerBaselineCostRangeRule(1.9, 2272.0),
        ),
    )
}
