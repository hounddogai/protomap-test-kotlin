package com.acme.transport.linehaul.laneplan.planbudget

/** A rule a seasonal lane plan estimate must pass. Check returns its problems, or none. */
interface SeasonalLanePlanEstimateRule {
    fun check(item: SeasonalLanePlanEstimate): MutableList<String>
}

data class SeasonalLanePlanEstimateCodePrefixRule(var prefix: String) : SeasonalLanePlanEstimateRule {
    override fun check(item: SeasonalLanePlanEstimate): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalLanePlanEstimateRequiredCodeRule(var message: String) : SeasonalLanePlanEstimateRule {
    override fun check(item: SeasonalLanePlanEstimate): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class SeasonalLanePlanEstimatePalletsRangeRule(var min: Int, var max: Int) : SeasonalLanePlanEstimateRule {
    override fun check(item: SeasonalLanePlanEstimate): MutableList<String> {
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

/** Runs every rule of a seasonal lane plan estimate. */
data class SeasonalLanePlanEstimateValidator(var rules: MutableList<SeasonalLanePlanEstimateRule>) {
    fun validate(item: SeasonalLanePlanEstimate): MutableList<String> {
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

    fun requireValid(item: SeasonalLanePlanEstimate) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<SeasonalLanePlanEstimate>): MutableMap<String, MutableList<String>> {
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

fun defaultSeasonalLanePlanEstimateValidator(): SeasonalLanePlanEstimateValidator {
    return SeasonalLanePlanEstimateValidator(
        mutableListOf<SeasonalLanePlanEstimateRule>(
            SeasonalLanePlanEstimateCodePrefixRule("Q"),
            SeasonalLanePlanEstimateRequiredCodeRule("seasonal lane plan estimate needs this field"),
            SeasonalLanePlanEstimatePalletsRangeRule(64, 86400),
        ),
    )
}
