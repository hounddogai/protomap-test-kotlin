package com.acme.transport.freightpay.ratecard.adjustment

/** A rule a rolling rate card policy must pass. Check returns its problems, or none. */
interface RollingRateCardPolicyRule {
    fun check(item: RollingRateCardPolicy): MutableList<String>
}

data class RollingRateCardPolicyNameLengthRule(var maxLength: Int) : RollingRateCardPolicyRule {
    override fun check(item: RollingRateCardPolicy): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RollingRateCardPolicyCodePrefixRule(var prefix: String) : RollingRateCardPolicyRule {
    override fun check(item: RollingRateCardPolicy): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class RollingRateCardPolicyRequiredNameRule(var message: String) : RollingRateCardPolicyRule {
    override fun check(item: RollingRateCardPolicy): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RollingRateCardPolicyGraceDaysRangeRule(var min: Int, var max: Int) : RollingRateCardPolicyRule {
    override fun check(item: RollingRateCardPolicy): MutableList<String> {
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

/** Runs every rule of a rolling rate card policy. */
data class RollingRateCardPolicyValidator(var rules: MutableList<RollingRateCardPolicyRule>) {
    fun validate(item: RollingRateCardPolicy): MutableList<String> {
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

    fun requireValid(item: RollingRateCardPolicy) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RollingRateCardPolicy>): MutableMap<String, MutableList<String>> {
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

fun defaultRollingRateCardPolicyValidator(): RollingRateCardPolicyValidator {
    return RollingRateCardPolicyValidator(
        mutableListOf<RollingRateCardPolicyRule>(
            RollingRateCardPolicyNameLengthRule(64),
            RollingRateCardPolicyCodePrefixRule("OPS"),
            RollingRateCardPolicyRequiredNameRule("rolling rate card policy needs a value"),
            RollingRateCardPolicyGraceDaysRangeRule(64, 1000),
        ),
    )
}
