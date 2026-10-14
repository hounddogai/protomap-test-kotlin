package com.acme.transport.emissions.idling.estimate

/** A rule a primary idling estimate must pass. Check returns its problems, or none. */
interface PrimaryIdlingEstimateRule {
    fun check(item: PrimaryIdlingEstimate): MutableList<String>
}

data class PrimaryIdlingEstimateAmountRangeRule(var min: Double, var max: Double) : PrimaryIdlingEstimateRule {
    override fun check(item: PrimaryIdlingEstimate): MutableList<String> {
        val value = item.amount
        if (value < min || value > max) {
            return mutableListOf<String>("amount $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryIdlingEstimateRequiredCodeRule(var message: String) : PrimaryIdlingEstimateRule {
    override fun check(item: PrimaryIdlingEstimate): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PrimaryIdlingEstimateRequiredCurrencyRule(var message: String) : PrimaryIdlingEstimateRule {
    override fun check(item: PrimaryIdlingEstimate): MutableList<String> {
        if (item.currency.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PrimaryIdlingEstimateExternalRefLengthRule(var maxLength: Int) : PrimaryIdlingEstimateRule {
    override fun check(item: PrimaryIdlingEstimate): MutableList<String> {
        val size = item.externalRef.length
        if (size > maxLength) {
            return mutableListOf<String>("external ref is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a primary idling estimate. */
data class PrimaryIdlingEstimateValidator(var rules: MutableList<PrimaryIdlingEstimateRule>) {
    fun validate(item: PrimaryIdlingEstimate): MutableList<String> {
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

    fun requireValid(item: PrimaryIdlingEstimate) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PrimaryIdlingEstimate>): MutableMap<String, MutableList<String>> {
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

fun defaultPrimaryIdlingEstimateValidator(): PrimaryIdlingEstimateValidator {
    return PrimaryIdlingEstimateValidator(
        mutableListOf<PrimaryIdlingEstimateRule>(
            PrimaryIdlingEstimateAmountRangeRule(3.6, 3785.8),
            PrimaryIdlingEstimateRequiredCodeRule("primary idling estimate needs an entry"),
            PrimaryIdlingEstimateRequiredCurrencyRule("primary idling estimate needs a value"),
            PrimaryIdlingEstimateExternalRefLengthRule(120),
        ),
    )
}
