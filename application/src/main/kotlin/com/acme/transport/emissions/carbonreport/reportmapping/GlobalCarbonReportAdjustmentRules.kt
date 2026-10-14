package com.acme.transport.emissions.carbonreport.reportmapping

/** A rule a global carbon report adjustment must pass. Check returns its problems, or none. */
interface GlobalCarbonReportAdjustmentRule {
    fun check(item: GlobalCarbonReportAdjustment): MutableList<String>
}

data class GlobalCarbonReportAdjustmentExpiresAtRangeRule(
    var min: Int,
    var max: Int,
) : GlobalCarbonReportAdjustmentRule {
    override fun check(item: GlobalCarbonReportAdjustment): MutableList<String> {
        val value = item.expiresAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("expires at $value is below $min")
        }
        if (value > max) {
            problems.add("expires at $value is above $max")
        }
        return problems
    }
}

data class GlobalCarbonReportAdjustmentRequiredNameRule(
    var message: String,
) : GlobalCarbonReportAdjustmentRule {
    override fun check(item: GlobalCarbonReportAdjustment): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class GlobalCarbonReportAdjustmentBudgetRangeRule(
    var min: Double,
    var max: Double,
) : GlobalCarbonReportAdjustmentRule {
    override fun check(item: GlobalCarbonReportAdjustment): MutableList<String> {
        val value = item.budget
        if (value < min || value > max) {
            return mutableListOf<String>("budget $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class GlobalCarbonReportAdjustmentMinimumRangeRule(
    var min: Double,
    var max: Double,
) : GlobalCarbonReportAdjustmentRule {
    override fun check(item: GlobalCarbonReportAdjustment): MutableList<String> {
        val value = item.minimum
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("minimum $value is below $min")
        }
        if (value > max) {
            problems.add("minimum $value is above $max")
        }
        return problems
    }
}

data class GlobalCarbonReportAdjustmentRequiredCodeRule(
    var message: String,
) : GlobalCarbonReportAdjustmentRule {
    override fun check(item: GlobalCarbonReportAdjustment): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a global carbon report adjustment. */
data class GlobalCarbonReportAdjustmentValidator(var rules: MutableList<GlobalCarbonReportAdjustmentRule>) {
    fun validate(item: GlobalCarbonReportAdjustment): MutableList<String> {
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

    fun requireValid(item: GlobalCarbonReportAdjustment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<GlobalCarbonReportAdjustment>): MutableMap<String, MutableList<String>> {
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

fun defaultGlobalCarbonReportAdjustmentValidator(): GlobalCarbonReportAdjustmentValidator {
    return GlobalCarbonReportAdjustmentValidator(
        mutableListOf<GlobalCarbonReportAdjustmentRule>(
            GlobalCarbonReportAdjustmentExpiresAtRangeRule(32, 255),
            GlobalCarbonReportAdjustmentRequiredNameRule("global carbon report adjustment needs an entry"),
            GlobalCarbonReportAdjustmentBudgetRangeRule(6.4, 4369.7),
            GlobalCarbonReportAdjustmentMinimumRangeRule(1.1, 2193.0),
            GlobalCarbonReportAdjustmentRequiredCodeRule("global carbon report adjustment needs an entry"),
        ),
    )
}
