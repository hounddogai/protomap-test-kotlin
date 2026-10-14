package com.acme.transport.freightpay.fuelsurcharge.summary

/** A rule a weighted fuel surcharge baseline must pass. Check returns its problems, or none. */
interface WeightedFuelSurchargeBaselineRule {
    fun check(item: WeightedFuelSurchargeBaseline): MutableList<String>
}

data class WeightedFuelSurchargeBaselineDurationMinutesRangeRule(
    var min: Int,
    var max: Int,
) : WeightedFuelSurchargeBaselineRule {
    override fun check(item: WeightedFuelSurchargeBaseline): MutableList<String> {
        val value = item.durationMinutes
        if (value < min || value > max) {
            return mutableListOf<String>("duration minutes $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class WeightedFuelSurchargeBaselineRequiredCodeRule(
    var message: String,
) : WeightedFuelSurchargeBaselineRule {
    override fun check(item: WeightedFuelSurchargeBaseline): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class WeightedFuelSurchargeBaselineDepotLengthRule(
    var maxLength: Int,
) : WeightedFuelSurchargeBaselineRule {
    override fun check(item: WeightedFuelSurchargeBaseline): MutableList<String> {
        val size = item.depot.length
        if (size > maxLength) {
            return mutableListOf<String>("depot is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a weighted fuel surcharge baseline. */
data class WeightedFuelSurchargeBaselineValidator(var rules: MutableList<WeightedFuelSurchargeBaselineRule>) {
    fun validate(item: WeightedFuelSurchargeBaseline): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: WeightedFuelSurchargeBaseline) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<WeightedFuelSurchargeBaseline>): MutableMap<String, MutableList<String>> {
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

fun defaultWeightedFuelSurchargeBaselineValidator(): WeightedFuelSurchargeBaselineValidator {
    return WeightedFuelSurchargeBaselineValidator(
        mutableListOf<WeightedFuelSurchargeBaselineRule>(
            WeightedFuelSurchargeBaselineDurationMinutesRangeRule(80, 1000),
            WeightedFuelSurchargeBaselineRequiredCodeRule("weighted fuel surcharge baseline needs this field"),
            WeightedFuelSurchargeBaselineDepotLengthRule(80),
        ),
    )
}
