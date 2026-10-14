package com.acme.transport.freightpay.ratecard.bundle

/** A rule a rate card bundle must pass. Check returns its problems, or none. */
interface RateCardBundleRule {
    fun check(item: RateCardBundle): MutableList<String>
}

data class RateCardBundleCapacityRangeRule(var min: Int, var max: Int) : RateCardBundleRule {
    override fun check(item: RateCardBundle): MutableList<String> {
        val value = item.capacity
        if (value < min || value > max) {
            return mutableListOf<String>("capacity $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class RateCardBundleRequiredNameRule(var message: String) : RateCardBundleRule {
    override fun check(item: RateCardBundle): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RateCardBundleNameLengthRule(var maxLength: Int) : RateCardBundleRule {
    override fun check(item: RateCardBundle): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RateCardBundleRequiredCategoryRule(var message: String) : RateCardBundleRule {
    override fun check(item: RateCardBundle): MutableList<String> {
        if (item.category.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RateCardBundleSequenceRangeRule(var min: Int, var max: Int) : RateCardBundleRule {
    override fun check(item: RateCardBundle): MutableList<String> {
        val value = item.sequence
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("sequence $value is below $min")
        }
        if (value > max) {
            problems.add("sequence $value is above $max")
        }
        return problems
    }
}

data class RateCardBundleDiscountRangeRule(var min: Double, var max: Double) : RateCardBundleRule {
    override fun check(item: RateCardBundle): MutableList<String> {
        val value = item.discount
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("discount $value is below $min")
        }
        if (value > max) {
            problems.add("discount $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a rate card bundle. */
data class RateCardBundleValidator(var rules: MutableList<RateCardBundleRule>) {
    fun validate(item: RateCardBundle): MutableList<String> {
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

    fun requireValid(item: RateCardBundle) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RateCardBundle>): MutableMap<String, MutableList<String>> {
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

fun defaultRateCardBundleValidator(): RateCardBundleValidator {
    return RateCardBundleValidator(
        mutableListOf<RateCardBundleRule>(
            RateCardBundleCapacityRangeRule(120, 255),
            RateCardBundleRequiredNameRule("rate card bundle needs this field"),
            RateCardBundleNameLengthRule(1000),
            RateCardBundleRequiredCategoryRule("rate card bundle needs an entry"),
            RateCardBundleSequenceRangeRule(80, 1000),
            RateCardBundleDiscountRangeRule(4.9, 704.8),
        ),
    )
}
