package com.acme.transport.freightpay.ratecard.review

/** A rule a rate card policy must pass. Check returns its problems, or none. */
interface RateCardPolicyRule {
    fun check(item: RateCardPolicy): MutableList<String>
}

data class RateCardPolicyRequiredTitleRule(var message: String) : RateCardPolicyRule {
    override fun check(item: RateCardPolicy): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RateCardPolicyStopCountRangeRule(var min: Int, var max: Int) : RateCardPolicyRule {
    override fun check(item: RateCardPolicy): MutableList<String> {
        val value = item.stopCount
        if (value < min || value > max) {
            return mutableListOf<String>("stop count $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class RateCardPolicyCodePrefixRule(var prefix: String) : RateCardPolicyRule {
    override fun check(item: RateCardPolicy): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class RateCardPolicyContactEmailLengthRule(var maxLength: Int) : RateCardPolicyRule {
    override fun check(item: RateCardPolicy): MutableList<String> {
        val size = item.contactEmail.length
        if (size > maxLength) {
            return mutableListOf<String>("contact email is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RateCardPolicyStartsAtRangeRule(var min: Int, var max: Int) : RateCardPolicyRule {
    override fun check(item: RateCardPolicy): MutableList<String> {
        val value = item.startsAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("starts at $value is below $min")
        }
        if (value > max) {
            problems.add("starts at $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a rate card policy. */
data class RateCardPolicyValidator(var rules: MutableList<RateCardPolicyRule>) {
    fun validate(item: RateCardPolicy): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RateCardPolicy) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RateCardPolicy>): MutableMap<String, MutableList<String>> {
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

fun defaultRateCardPolicyValidator(): RateCardPolicyValidator {
    return RateCardPolicyValidator(
        mutableListOf<RateCardPolicyRule>(
            RateCardPolicyRequiredTitleRule("rate card policy needs this field"),
            RateCardPolicyStopCountRangeRule(80, 3600),
            RateCardPolicyCodePrefixRule("REF"),
            RateCardPolicyContactEmailLengthRule(120),
            RateCardPolicyStartsAtRangeRule(16, 32),
        ),
    )
}
