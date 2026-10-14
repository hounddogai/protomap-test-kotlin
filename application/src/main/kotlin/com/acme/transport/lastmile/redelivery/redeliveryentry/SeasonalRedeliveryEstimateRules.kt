package com.acme.transport.lastmile.redelivery.redeliveryentry

/** A rule a seasonal redelivery estimate must pass. Check returns its problems, or none. */
interface SeasonalRedeliveryEstimateRule {
    fun check(item: SeasonalRedeliveryEstimate): MutableList<String>
}

data class SeasonalRedeliveryEstimateLabelLengthRule(var maxLength: Int) : SeasonalRedeliveryEstimateRule {
    override fun check(item: SeasonalRedeliveryEstimate): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalRedeliveryEstimateRequiredCodeRule(var message: String) : SeasonalRedeliveryEstimateRule {
    override fun check(item: SeasonalRedeliveryEstimate): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class SeasonalRedeliveryEstimateRequiredSummaryRule(var message: String) : SeasonalRedeliveryEstimateRule {
    override fun check(item: SeasonalRedeliveryEstimate): MutableList<String> {
        if (item.summary.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class SeasonalRedeliveryEstimateRequiredOwnerIdRule(var message: String) : SeasonalRedeliveryEstimateRule {
    override fun check(item: SeasonalRedeliveryEstimate): MutableList<String> {
        if (item.ownerId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class SeasonalRedeliveryEstimateQuantityRangeRule(
    var min: Int,
    var max: Int,
) : SeasonalRedeliveryEstimateRule {
    override fun check(item: SeasonalRedeliveryEstimate): MutableList<String> {
        val value = item.quantity
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("quantity $value is below $min")
        }
        if (value > max) {
            problems.add("quantity $value is above $max")
        }
        return problems
    }
}

data class SeasonalRedeliveryEstimateCodeLengthRule(var maxLength: Int) : SeasonalRedeliveryEstimateRule {
    override fun check(item: SeasonalRedeliveryEstimate): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a seasonal redelivery estimate. */
data class SeasonalRedeliveryEstimateValidator(var rules: MutableList<SeasonalRedeliveryEstimateRule>) {
    fun validate(item: SeasonalRedeliveryEstimate): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: SeasonalRedeliveryEstimate) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<SeasonalRedeliveryEstimate>): MutableMap<String, MutableList<String>> {
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

fun defaultSeasonalRedeliveryEstimateValidator(): SeasonalRedeliveryEstimateValidator {
    return SeasonalRedeliveryEstimateValidator(
        mutableListOf<SeasonalRedeliveryEstimateRule>(
            SeasonalRedeliveryEstimateLabelLengthRule(1000),
            SeasonalRedeliveryEstimateRequiredCodeRule("seasonal redelivery estimate needs this field"),
            SeasonalRedeliveryEstimateRequiredSummaryRule("seasonal redelivery estimate needs this field"),
            SeasonalRedeliveryEstimateRequiredOwnerIdRule("seasonal redelivery estimate needs an entry"),
            SeasonalRedeliveryEstimateQuantityRangeRule(32, 120),
            SeasonalRedeliveryEstimateCodeLengthRule(86400),
        ),
    )
}
