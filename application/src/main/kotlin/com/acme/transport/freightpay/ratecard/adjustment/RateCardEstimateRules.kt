package com.acme.transport.freightpay.ratecard.adjustment

/** A rule a rate card estimate must pass. Check returns its problems, or none. */
interface RateCardEstimateRule {
    fun check(item: RateCardEstimate): MutableList<String>
}

data class RateCardEstimateNameLengthRule(var maxLength: Int) : RateCardEstimateRule {
    override fun check(item: RateCardEstimate): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RateCardEstimatePriorityRangeRule(var min: Int, var max: Int) : RateCardEstimateRule {
    override fun check(item: RateCardEstimate): MutableList<String> {
        val value = item.priority
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("priority $value is below $min")
        }
        if (value > max) {
            problems.add("priority $value is above $max")
        }
        return problems
    }
}

data class RateCardEstimateRequiredSegmentRule(var message: String) : RateCardEstimateRule {
    override fun check(item: RateCardEstimate): MutableList<String> {
        if (item.segment.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RateCardEstimateMaximumRangeRule(var min: Double, var max: Double) : RateCardEstimateRule {
    override fun check(item: RateCardEstimate): MutableList<String> {
        val value = item.maximum
        if (value < min || value > max) {
            return mutableListOf<String>("maximum $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a rate card estimate. */
data class RateCardEstimateValidator(var rules: MutableList<RateCardEstimateRule>) {
    fun validate(item: RateCardEstimate): MutableList<String> {
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

    fun requireValid(item: RateCardEstimate) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RateCardEstimate>): MutableMap<String, MutableList<String>> {
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

fun defaultRateCardEstimateValidator(): RateCardEstimateValidator {
    return RateCardEstimateValidator(
        mutableListOf<RateCardEstimateRule>(
            RateCardEstimateNameLengthRule(3600),
            RateCardEstimatePriorityRangeRule(1000, 3600),
            RateCardEstimateRequiredSegmentRule("rate card estimate needs this field"),
            RateCardEstimateMaximumRangeRule(7.5, 3528.7),
        ),
    )
}
