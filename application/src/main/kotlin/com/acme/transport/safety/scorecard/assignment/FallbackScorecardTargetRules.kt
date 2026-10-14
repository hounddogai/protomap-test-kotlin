package com.acme.transport.safety.scorecard.assignment

/** A rule a fallback scorecard target must pass. Check returns its problems, or none. */
interface FallbackScorecardTargetRule {
    fun check(item: FallbackScorecardTarget): MutableList<String>
}

data class FallbackScorecardTargetLoadKgRangeRule(
    var min: Double,
    var max: Double,
) : FallbackScorecardTargetRule {
    override fun check(item: FallbackScorecardTarget): MutableList<String> {
        val value = item.loadKg
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("load kg $value is below $min")
        }
        if (value > max) {
            problems.add("load kg $value is above $max")
        }
        return problems
    }
}

data class FallbackScorecardTargetDepotLengthRule(var maxLength: Int) : FallbackScorecardTargetRule {
    override fun check(item: FallbackScorecardTarget): MutableList<String> {
        val size = item.depot.length
        if (size > maxLength) {
            return mutableListOf<String>("depot is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FallbackScorecardTargetRequiredCurrencyRule(var message: String) : FallbackScorecardTargetRule {
    override fun check(item: FallbackScorecardTarget): MutableList<String> {
        if (item.currency.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FallbackScorecardTargetStartsAtRangeRule(var min: Int, var max: Int) : FallbackScorecardTargetRule {
    override fun check(item: FallbackScorecardTarget): MutableList<String> {
        val value = item.startsAt
        if (value < min || value > max) {
            return mutableListOf<String>("starts at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class FallbackScorecardTargetCodeLengthRule(var maxLength: Int) : FallbackScorecardTargetRule {
    override fun check(item: FallbackScorecardTarget): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FallbackScorecardTargetCostRangeRule(
    var min: Double,
    var max: Double,
) : FallbackScorecardTargetRule {
    override fun check(item: FallbackScorecardTarget): MutableList<String> {
        val value = item.cost
        if (value < min || value > max) {
            return mutableListOf<String>("cost $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a fallback scorecard target. */
data class FallbackScorecardTargetValidator(var rules: MutableList<FallbackScorecardTargetRule>) {
    fun validate(item: FallbackScorecardTarget): MutableList<String> {
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

    fun requireValid(item: FallbackScorecardTarget) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FallbackScorecardTarget>): MutableMap<String, MutableList<String>> {
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

fun defaultFallbackScorecardTargetValidator(): FallbackScorecardTargetValidator {
    return FallbackScorecardTargetValidator(
        mutableListOf<FallbackScorecardTargetRule>(
            FallbackScorecardTargetLoadKgRangeRule(5.7, 1620.3),
            FallbackScorecardTargetDepotLengthRule(120),
            FallbackScorecardTargetRequiredCurrencyRule("fallback scorecard target needs a value"),
            FallbackScorecardTargetStartsAtRangeRule(32, 120),
            FallbackScorecardTargetCodeLengthRule(86400),
            FallbackScorecardTargetCostRangeRule(0.4, 4128.8),
        ),
    )
}
