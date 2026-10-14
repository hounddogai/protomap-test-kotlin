package com.acme.transport.linehaul.backhaul.adjustment

/** A rule a primary backhaul estimate must pass. Check returns its problems, or none. */
interface PrimaryBackhaulEstimateRule {
    fun check(item: PrimaryBackhaulEstimate): MutableList<String>
}

data class PrimaryBackhaulEstimateRequiredTrailerIdRule(var message: String) : PrimaryBackhaulEstimateRule {
    override fun check(item: PrimaryBackhaulEstimate): MutableList<String> {
        if (item.trailerId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PrimaryBackhaulEstimatePriorityRangeRule(var min: Int, var max: Int) : PrimaryBackhaulEstimateRule {
    override fun check(item: PrimaryBackhaulEstimate): MutableList<String> {
        val value = item.priority
        if (value < min || value > max) {
            return mutableListOf<String>("priority $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryBackhaulEstimateFuelLitersRangeRule(
    var min: Double,
    var max: Double,
) : PrimaryBackhaulEstimateRule {
    override fun check(item: PrimaryBackhaulEstimate): MutableList<String> {
        val value = item.fuelLiters
        if (value < min || value > max) {
            return mutableListOf<String>("fuel liters $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryBackhaulEstimateRequiredCurrencyRule(var message: String) : PrimaryBackhaulEstimateRule {
    override fun check(item: PrimaryBackhaulEstimate): MutableList<String> {
        if (item.currency.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PrimaryBackhaulEstimateCodePrefixRule(var prefix: String) : PrimaryBackhaulEstimateRule {
    override fun check(item: PrimaryBackhaulEstimate): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a primary backhaul estimate. */
data class PrimaryBackhaulEstimateValidator(var rules: MutableList<PrimaryBackhaulEstimateRule>) {
    fun validate(item: PrimaryBackhaulEstimate): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: PrimaryBackhaulEstimate) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PrimaryBackhaulEstimate>): MutableMap<String, MutableList<String>> {
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

fun defaultPrimaryBackhaulEstimateValidator(): PrimaryBackhaulEstimateValidator {
    return PrimaryBackhaulEstimateValidator(
        mutableListOf<PrimaryBackhaulEstimateRule>(
            PrimaryBackhaulEstimateRequiredTrailerIdRule("primary backhaul estimate needs a value"),
            PrimaryBackhaulEstimatePriorityRangeRule(255, 1000),
            PrimaryBackhaulEstimateFuelLitersRangeRule(4.7, 2818.3),
            PrimaryBackhaulEstimateRequiredCurrencyRule("primary backhaul estimate needs a value"),
            PrimaryBackhaulEstimateCodePrefixRule("ACM"),
        ),
    )
}
