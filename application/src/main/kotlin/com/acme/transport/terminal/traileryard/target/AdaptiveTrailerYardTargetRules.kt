package com.acme.transport.terminal.traileryard.target

/** A rule a adaptive trailer yard target must pass. Check returns its problems, or none. */
interface AdaptiveTrailerYardTargetRule {
    fun check(item: AdaptiveTrailerYardTarget): MutableList<String>
}

data class AdaptiveTrailerYardTargetLocaleLengthRule(var maxLength: Int) : AdaptiveTrailerYardTargetRule {
    override fun check(item: AdaptiveTrailerYardTarget): MutableList<String> {
        val size = item.locale.length
        if (size > maxLength) {
            return mutableListOf<String>("locale is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveTrailerYardTargetRequiredCodeRule(var message: String) : AdaptiveTrailerYardTargetRule {
    override fun check(item: AdaptiveTrailerYardTarget): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveTrailerYardTargetStartsAtRangeRule(
    var min: Int,
    var max: Int,
) : AdaptiveTrailerYardTargetRule {
    override fun check(item: AdaptiveTrailerYardTarget): MutableList<String> {
        val value = item.startsAt
        if (value < min || value > max) {
            return mutableListOf<String>("starts at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveTrailerYardTargetRatioRangeRule(
    var min: Double,
    var max: Double,
) : AdaptiveTrailerYardTargetRule {
    override fun check(item: AdaptiveTrailerYardTarget): MutableList<String> {
        val value = item.ratio
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("ratio $value is below $min")
        }
        if (value > max) {
            problems.add("ratio $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a adaptive trailer yard target. */
data class AdaptiveTrailerYardTargetValidator(var rules: MutableList<AdaptiveTrailerYardTargetRule>) {
    fun validate(item: AdaptiveTrailerYardTarget): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: AdaptiveTrailerYardTarget) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AdaptiveTrailerYardTarget>): MutableMap<String, MutableList<String>> {
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

fun defaultAdaptiveTrailerYardTargetValidator(): AdaptiveTrailerYardTargetValidator {
    return AdaptiveTrailerYardTargetValidator(
        mutableListOf<AdaptiveTrailerYardTargetRule>(
            AdaptiveTrailerYardTargetLocaleLengthRule(86400),
            AdaptiveTrailerYardTargetRequiredCodeRule("adaptive trailer yard target needs an entry"),
            AdaptiveTrailerYardTargetStartsAtRangeRule(120, 86400),
            AdaptiveTrailerYardTargetRatioRangeRule(0.4, 3492.9),
        ),
    )
}
