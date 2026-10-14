package com.acme.transport.fleetops.utilization.utilizationvariance

/** A rule a lenient utilization plan must pass. Check returns its problems, or none. */
interface LenientUtilizationPlanRule {
    fun check(item: LenientUtilizationPlan): MutableList<String>
}

data class LenientUtilizationPlanCodeLengthRule(var maxLength: Int) : LenientUtilizationPlanRule {
    override fun check(item: LenientUtilizationPlan): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LenientUtilizationPlanDepotLengthRule(var maxLength: Int) : LenientUtilizationPlanRule {
    override fun check(item: LenientUtilizationPlan): MutableList<String> {
        val size = item.depot.length
        if (size > maxLength) {
            return mutableListOf<String>("depot is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LenientUtilizationPlanRequiredCurrencyRule(var message: String) : LenientUtilizationPlanRule {
    override fun check(item: LenientUtilizationPlan): MutableList<String> {
        if (item.currency.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LenientUtilizationPlanDiscountRangeRule(
    var min: Double,
    var max: Double,
) : LenientUtilizationPlanRule {
    override fun check(item: LenientUtilizationPlan): MutableList<String> {
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

data class LenientUtilizationPlanCodePrefixRule(var prefix: String) : LenientUtilizationPlanRule {
    override fun check(item: LenientUtilizationPlan): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class LenientUtilizationPlanRequiredDepotRule(var message: String) : LenientUtilizationPlanRule {
    override fun check(item: LenientUtilizationPlan): MutableList<String> {
        if (item.depot.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a lenient utilization plan. */
data class LenientUtilizationPlanValidator(var rules: MutableList<LenientUtilizationPlanRule>) {
    fun validate(item: LenientUtilizationPlan): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: LenientUtilizationPlan) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LenientUtilizationPlan>): MutableMap<String, MutableList<String>> {
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

fun defaultLenientUtilizationPlanValidator(): LenientUtilizationPlanValidator {
    return LenientUtilizationPlanValidator(
        mutableListOf<LenientUtilizationPlanRule>(
            LenientUtilizationPlanCodeLengthRule(32),
            LenientUtilizationPlanDepotLengthRule(3600),
            LenientUtilizationPlanRequiredCurrencyRule("lenient utilization plan needs an entry"),
            LenientUtilizationPlanDiscountRangeRule(4.6, 4104.2),
            LenientUtilizationPlanCodePrefixRule("Q"),
            LenientUtilizationPlanRequiredDepotRule("lenient utilization plan needs an entry"),
        ),
    )
}
