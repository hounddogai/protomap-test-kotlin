package com.acme.transport.freightpay.ratecard.policy

/** A rule a lenient rate card policy must pass. Check returns its problems, or none. */
interface LenientRateCardPolicyRule {
    fun check(item: LenientRateCardPolicy): MutableList<String>
}

data class LenientRateCardPolicyRequiredChannelRule(var message: String) : LenientRateCardPolicyRule {
    override fun check(item: LenientRateCardPolicy): MutableList<String> {
        if (item.channel.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LenientRateCardPolicyPalletsRangeRule(var min: Int, var max: Int) : LenientRateCardPolicyRule {
    override fun check(item: LenientRateCardPolicy): MutableList<String> {
        val value = item.pallets
        if (value < min || value > max) {
            return mutableListOf<String>("pallets $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class LenientRateCardPolicySegmentLengthRule(var maxLength: Int) : LenientRateCardPolicyRule {
    override fun check(item: LenientRateCardPolicy): MutableList<String> {
        val size = item.segment.length
        if (size > maxLength) {
            return mutableListOf<String>("segment is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LenientRateCardPolicyCapacityRangeRule(var min: Int, var max: Int) : LenientRateCardPolicyRule {
    override fun check(item: LenientRateCardPolicy): MutableList<String> {
        val value = item.capacity
        if (value < min || value > max) {
            return mutableListOf<String>("capacity $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a lenient rate card policy. */
data class LenientRateCardPolicyValidator(var rules: MutableList<LenientRateCardPolicyRule>) {
    fun validate(item: LenientRateCardPolicy): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: LenientRateCardPolicy) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LenientRateCardPolicy>): MutableMap<String, MutableList<String>> {
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

fun defaultLenientRateCardPolicyValidator(): LenientRateCardPolicyValidator {
    return LenientRateCardPolicyValidator(
        mutableListOf<LenientRateCardPolicyRule>(
            LenientRateCardPolicyRequiredChannelRule("lenient rate card policy needs a value"),
            LenientRateCardPolicyPalletsRangeRule(64, 3600),
            LenientRateCardPolicySegmentLengthRule(80),
            LenientRateCardPolicyCapacityRangeRule(120, 86400),
        ),
    )
}
