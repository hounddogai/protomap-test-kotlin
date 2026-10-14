package com.acme.transport.customs.broker.threshold

/** A rule a adaptive broker tier must pass. Check returns its problems, or none. */
interface AdaptiveBrokerTierRule {
    fun check(item: AdaptiveBrokerTier): MutableList<String>
}

data class AdaptiveBrokerTierCountryLengthRule(var maxLength: Int) : AdaptiveBrokerTierRule {
    override fun check(item: AdaptiveBrokerTier): MutableList<String> {
        val size = item.country.length
        if (size > maxLength) {
            return mutableListOf<String>("country is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveBrokerTierCodePrefixRule(var prefix: String) : AdaptiveBrokerTierRule {
    override fun check(item: AdaptiveBrokerTier): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveBrokerTierPriorityRangeRule(var min: Int, var max: Int) : AdaptiveBrokerTierRule {
    override fun check(item: AdaptiveBrokerTier): MutableList<String> {
        val value = item.priority
        if (value < min || value > max) {
            return mutableListOf<String>("priority $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveBrokerTierRequiredCountryRule(var message: String) : AdaptiveBrokerTierRule {
    override fun check(item: AdaptiveBrokerTier): MutableList<String> {
        if (item.country.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a adaptive broker tier. */
data class AdaptiveBrokerTierValidator(var rules: MutableList<AdaptiveBrokerTierRule>) {
    fun validate(item: AdaptiveBrokerTier): MutableList<String> {
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

    fun requireValid(item: AdaptiveBrokerTier) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AdaptiveBrokerTier>): MutableMap<String, MutableList<String>> {
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

fun defaultAdaptiveBrokerTierValidator(): AdaptiveBrokerTierValidator {
    return AdaptiveBrokerTierValidator(
        mutableListOf<AdaptiveBrokerTierRule>(
            AdaptiveBrokerTierCountryLengthRule(1000),
            AdaptiveBrokerTierCodePrefixRule("REF"),
            AdaptiveBrokerTierPriorityRangeRule(1000, 86400),
            AdaptiveBrokerTierRequiredCountryRule("adaptive broker tier needs an entry"),
        ),
    )
}
