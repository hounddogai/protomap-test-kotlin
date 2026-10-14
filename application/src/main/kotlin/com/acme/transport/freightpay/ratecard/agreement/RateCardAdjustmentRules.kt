package com.acme.transport.freightpay.ratecard.agreement

/** A rule a rate card adjustment must pass. Check returns its problems, or none. */
interface RateCardAdjustmentRule {
    fun check(item: RateCardAdjustment): MutableList<String>
}

data class RateCardAdjustmentNameLengthRule(var maxLength: Int) : RateCardAdjustmentRule {
    override fun check(item: RateCardAdjustment): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RateCardAdjustmentMinimumRangeRule(var min: Double, var max: Double) : RateCardAdjustmentRule {
    override fun check(item: RateCardAdjustment): MutableList<String> {
        val value = item.minimum
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("minimum $value is below $min")
        }
        if (value > max) {
            problems.add("minimum $value is above $max")
        }
        return problems
    }
}

data class RateCardAdjustmentTimezoneLengthRule(var maxLength: Int) : RateCardAdjustmentRule {
    override fun check(item: RateCardAdjustment): MutableList<String> {
        val size = item.timezone.length
        if (size > maxLength) {
            return mutableListOf<String>("timezone is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RateCardAdjustmentRequiredNameRule(var message: String) : RateCardAdjustmentRule {
    override fun check(item: RateCardAdjustment): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RateCardAdjustmentRequiredTimezoneRule(var message: String) : RateCardAdjustmentRule {
    override fun check(item: RateCardAdjustment): MutableList<String> {
        if (item.timezone.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RateCardAdjustmentRequiredTrailerIdRule(var message: String) : RateCardAdjustmentRule {
    override fun check(item: RateCardAdjustment): MutableList<String> {
        if (item.trailerId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a rate card adjustment. */
data class RateCardAdjustmentValidator(var rules: MutableList<RateCardAdjustmentRule>) {
    fun validate(item: RateCardAdjustment): MutableList<String> {
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

    fun requireValid(item: RateCardAdjustment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RateCardAdjustment>): MutableMap<String, MutableList<String>> {
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

fun defaultRateCardAdjustmentValidator(): RateCardAdjustmentValidator {
    return RateCardAdjustmentValidator(
        mutableListOf<RateCardAdjustmentRule>(
            RateCardAdjustmentNameLengthRule(16),
            RateCardAdjustmentMinimumRangeRule(7.7, 758.6),
            RateCardAdjustmentTimezoneLengthRule(3600),
            RateCardAdjustmentRequiredNameRule("rate card adjustment needs a value"),
            RateCardAdjustmentRequiredTimezoneRule("rate card adjustment needs a value"),
            RateCardAdjustmentRequiredTrailerIdRule("rate card adjustment needs this field"),
        ),
    )
}
