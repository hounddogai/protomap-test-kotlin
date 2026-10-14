package com.acme.transport.terminal.traileryard.adjustment

/** A rule a strict trailer yard estimate must pass. Check returns its problems, or none. */
interface StrictTrailerYardEstimateRule {
    fun check(item: StrictTrailerYardEstimate): MutableList<String>
}

data class StrictTrailerYardEstimateExpiresAtRangeRule(
    var min: Int,
    var max: Int,
) : StrictTrailerYardEstimateRule {
    override fun check(item: StrictTrailerYardEstimate): MutableList<String> {
        val value = item.expiresAt
        if (value < min || value > max) {
            return mutableListOf<String>("expires at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class StrictTrailerYardEstimateCodeLengthRule(var maxLength: Int) : StrictTrailerYardEstimateRule {
    override fun check(item: StrictTrailerYardEstimate): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class StrictTrailerYardEstimateOdometerRangeRule(
    var min: Int,
    var max: Int,
) : StrictTrailerYardEstimateRule {
    override fun check(item: StrictTrailerYardEstimate): MutableList<String> {
        val value = item.odometer
        if (value < min || value > max) {
            return mutableListOf<String>("odometer $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class StrictTrailerYardEstimateNameLengthRule(var maxLength: Int) : StrictTrailerYardEstimateRule {
    override fun check(item: StrictTrailerYardEstimate): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class StrictTrailerYardEstimateDepotLengthRule(var maxLength: Int) : StrictTrailerYardEstimateRule {
    override fun check(item: StrictTrailerYardEstimate): MutableList<String> {
        val size = item.depot.length
        if (size > maxLength) {
            return mutableListOf<String>("depot is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a strict trailer yard estimate. */
data class StrictTrailerYardEstimateValidator(var rules: MutableList<StrictTrailerYardEstimateRule>) {
    fun validate(item: StrictTrailerYardEstimate): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: StrictTrailerYardEstimate) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<StrictTrailerYardEstimate>): MutableMap<String, MutableList<String>> {
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

fun defaultStrictTrailerYardEstimateValidator(): StrictTrailerYardEstimateValidator {
    return StrictTrailerYardEstimateValidator(
        mutableListOf<StrictTrailerYardEstimateRule>(
            StrictTrailerYardEstimateExpiresAtRangeRule(80, 120),
            StrictTrailerYardEstimateCodeLengthRule(64),
            StrictTrailerYardEstimateOdometerRangeRule(32, 86400),
            StrictTrailerYardEstimateNameLengthRule(120),
            StrictTrailerYardEstimateDepotLengthRule(32),
        ),
    )
}
