package com.acme.transport.safety.nearmiss.missrule

/** A rule a adaptive near miss quota must pass. Check returns its problems, or none. */
interface AdaptiveNearMissQuotaRule {
    fun check(item: AdaptiveNearMissQuota): MutableList<String>
}

data class AdaptiveNearMissQuotaCodePrefixRule(var prefix: String) : AdaptiveNearMissQuotaRule {
    override fun check(item: AdaptiveNearMissQuota): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveNearMissQuotaCodeLengthRule(var maxLength: Int) : AdaptiveNearMissQuotaRule {
    override fun check(item: AdaptiveNearMissQuota): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveNearMissQuotaWeightRangeRule(var min: Double, var max: Double) : AdaptiveNearMissQuotaRule {
    override fun check(item: AdaptiveNearMissQuota): MutableList<String> {
        val value = item.weight
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("weight $value is below $min")
        }
        if (value > max) {
            problems.add("weight $value is above $max")
        }
        return problems
    }
}

data class AdaptiveNearMissQuotaRateRangeRule(var min: Double, var max: Double) : AdaptiveNearMissQuotaRule {
    override fun check(item: AdaptiveNearMissQuota): MutableList<String> {
        val value = item.rate
        if (value < min || value > max) {
            return mutableListOf<String>("rate $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a adaptive near miss quota. */
data class AdaptiveNearMissQuotaValidator(var rules: MutableList<AdaptiveNearMissQuotaRule>) {
    fun validate(item: AdaptiveNearMissQuota): MutableList<String> {
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

    fun requireValid(item: AdaptiveNearMissQuota) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AdaptiveNearMissQuota>): MutableMap<String, MutableList<String>> {
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

fun defaultAdaptiveNearMissQuotaValidator(): AdaptiveNearMissQuotaValidator {
    return AdaptiveNearMissQuotaValidator(
        mutableListOf<AdaptiveNearMissQuotaRule>(
            AdaptiveNearMissQuotaCodePrefixRule("X"),
            AdaptiveNearMissQuotaCodeLengthRule(80),
            AdaptiveNearMissQuotaWeightRangeRule(9.7, 3535.8),
            AdaptiveNearMissQuotaRateRangeRule(7.5, 3262.4),
        ),
    )
}
