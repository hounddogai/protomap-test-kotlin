package com.acme.transport.customs.hold.estimate

/** A rule a weighted hold variance must pass. Check returns its problems, or none. */
interface WeightedHoldVarianceRule {
    fun check(item: WeightedHoldVariance): MutableList<String>
}

data class WeightedHoldVarianceCodeLengthRule(var maxLength: Int) : WeightedHoldVarianceRule {
    override fun check(item: WeightedHoldVariance): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class WeightedHoldVarianceAmountRangeRule(var min: Double, var max: Double) : WeightedHoldVarianceRule {
    override fun check(item: WeightedHoldVariance): MutableList<String> {
        val value = item.amount
        if (value < min || value > max) {
            return mutableListOf<String>("amount $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class WeightedHoldVarianceGraceDaysRangeRule(var min: Int, var max: Int) : WeightedHoldVarianceRule {
    override fun check(item: WeightedHoldVariance): MutableList<String> {
        val value = item.graceDays
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("grace days $value is below $min")
        }
        if (value > max) {
            problems.add("grace days $value is above $max")
        }
        return problems
    }
}

data class WeightedHoldVarianceRequiredLabelRule(var message: String) : WeightedHoldVarianceRule {
    override fun check(item: WeightedHoldVariance): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a weighted hold variance. */
data class WeightedHoldVarianceValidator(var rules: MutableList<WeightedHoldVarianceRule>) {
    fun validate(item: WeightedHoldVariance): MutableList<String> {
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

    fun requireValid(item: WeightedHoldVariance) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<WeightedHoldVariance>): MutableMap<String, MutableList<String>> {
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

fun defaultWeightedHoldVarianceValidator(): WeightedHoldVarianceValidator {
    return WeightedHoldVarianceValidator(
        mutableListOf<WeightedHoldVarianceRule>(
            WeightedHoldVarianceCodeLengthRule(80),
            WeightedHoldVarianceAmountRangeRule(6.7, 1868.7),
            WeightedHoldVarianceGraceDaysRangeRule(32, 3600),
            WeightedHoldVarianceRequiredLabelRule("weighted hold variance needs an entry"),
        ),
    )
}
