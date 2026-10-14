package com.acme.transport.emissions.fuelburn.rule

/** A rule a regional fuel burn rule must pass. Check returns its problems, or none. */
interface RegionalFuelBurnRuleRule {
    fun check(item: RegionalFuelBurnRule): MutableList<String>
}

data class RegionalFuelBurnRuleCodePrefixRule(var prefix: String) : RegionalFuelBurnRuleRule {
    override fun check(item: RegionalFuelBurnRule): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class RegionalFuelBurnRuleRequiredTitleRule(var message: String) : RegionalFuelBurnRuleRule {
    override fun check(item: RegionalFuelBurnRule): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RegionalFuelBurnRuleRatioRangeRule(var min: Double, var max: Double) : RegionalFuelBurnRuleRule {
    override fun check(item: RegionalFuelBurnRule): MutableList<String> {
        val value = item.ratio
        if (value < min || value > max) {
            return mutableListOf<String>("ratio $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class RegionalFuelBurnRuleScoreRangeRule(var min: Double, var max: Double) : RegionalFuelBurnRuleRule {
    override fun check(item: RegionalFuelBurnRule): MutableList<String> {
        val value = item.score
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("score $value is below $min")
        }
        if (value > max) {
            problems.add("score $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a regional fuel burn rule. */
data class RegionalFuelBurnRuleValidator(var rules: MutableList<RegionalFuelBurnRuleRule>) {
    fun validate(item: RegionalFuelBurnRule): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RegionalFuelBurnRule) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RegionalFuelBurnRule>): MutableMap<String, MutableList<String>> {
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

fun defaultRegionalFuelBurnRuleValidator(): RegionalFuelBurnRuleValidator {
    return RegionalFuelBurnRuleValidator(
        mutableListOf<RegionalFuelBurnRuleRule>(
            RegionalFuelBurnRuleCodePrefixRule("OPS"),
            RegionalFuelBurnRuleRequiredTitleRule("regional fuel burn rule needs a value"),
            RegionalFuelBurnRuleRatioRangeRule(2.7, 3639.1),
            RegionalFuelBurnRuleScoreRangeRule(2.4, 327.5),
        ),
    )
}
