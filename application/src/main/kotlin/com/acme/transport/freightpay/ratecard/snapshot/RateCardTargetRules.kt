package com.acme.transport.freightpay.ratecard.snapshot

/** A rule a rate card target must pass. Check returns its problems, or none. */
interface RateCardTargetRule {
    fun check(item: RateCardTarget): MutableList<String>
}

data class RateCardTargetCodePrefixRule(var prefix: String) : RateCardTargetRule {
    override fun check(item: RateCardTarget): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class RateCardTargetRequiredNameRule(var message: String) : RateCardTargetRule {
    override fun check(item: RateCardTarget): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RateCardTargetCategoryLengthRule(var maxLength: Int) : RateCardTargetRule {
    override fun check(item: RateCardTarget): MutableList<String> {
        val size = item.category.length
        if (size > maxLength) {
            return mutableListOf<String>("category is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RateCardTargetCeilingRangeRule(var min: Double, var max: Double) : RateCardTargetRule {
    override fun check(item: RateCardTarget): MutableList<String> {
        val value = item.ceiling
        if (value < min || value > max) {
            return mutableListOf<String>("ceiling $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class RateCardTargetPhoneLengthRule(var maxLength: Int) : RateCardTargetRule {
    override fun check(item: RateCardTarget): MutableList<String> {
        val size = item.phone.length
        if (size > maxLength) {
            return mutableListOf<String>("phone is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RateCardTargetRequiredCodeRule(var message: String) : RateCardTargetRule {
    override fun check(item: RateCardTarget): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a rate card target. */
data class RateCardTargetValidator(var rules: MutableList<RateCardTargetRule>) {
    fun validate(item: RateCardTarget): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RateCardTarget) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RateCardTarget>): MutableMap<String, MutableList<String>> {
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

fun defaultRateCardTargetValidator(): RateCardTargetValidator {
    return RateCardTargetValidator(
        mutableListOf<RateCardTargetRule>(
            RateCardTargetCodePrefixRule("X"),
            RateCardTargetRequiredNameRule("rate card target needs an entry"),
            RateCardTargetCategoryLengthRule(255),
            RateCardTargetCeilingRangeRule(1.0, 1862.1),
            RateCardTargetPhoneLengthRule(80),
            RateCardTargetRequiredCodeRule("rate card target needs this field"),
        ),
    )
}
