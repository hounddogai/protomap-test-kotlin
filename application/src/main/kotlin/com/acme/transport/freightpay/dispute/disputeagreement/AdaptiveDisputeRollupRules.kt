package com.acme.transport.freightpay.dispute.disputeagreement

/** A rule a adaptive dispute rollup must pass. Check returns its problems, or none. */
interface AdaptiveDisputeRollupRule {
    fun check(item: AdaptiveDisputeRollup): MutableList<String>
}

data class AdaptiveDisputeRollupCarrierLengthRule(var maxLength: Int) : AdaptiveDisputeRollupRule {
    override fun check(item: AdaptiveDisputeRollup): MutableList<String> {
        val size = item.carrier.length
        if (size > maxLength) {
            return mutableListOf<String>("carrier is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveDisputeRollupCodeLengthRule(var maxLength: Int) : AdaptiveDisputeRollupRule {
    override fun check(item: AdaptiveDisputeRollup): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveDisputeRollupRequiredTitleRule(var message: String) : AdaptiveDisputeRollupRule {
    override fun check(item: AdaptiveDisputeRollup): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveDisputeRollupThresholdRangeRule(
    var min: Double,
    var max: Double,
) : AdaptiveDisputeRollupRule {
    override fun check(item: AdaptiveDisputeRollup): MutableList<String> {
        val value = item.threshold
        if (value < min || value > max) {
            return mutableListOf<String>("threshold $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveDisputeRollupTitleLengthRule(var maxLength: Int) : AdaptiveDisputeRollupRule {
    override fun check(item: AdaptiveDisputeRollup): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveDisputeRollupCodePrefixRule(var prefix: String) : AdaptiveDisputeRollupRule {
    override fun check(item: AdaptiveDisputeRollup): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a adaptive dispute rollup. */
data class AdaptiveDisputeRollupValidator(var rules: MutableList<AdaptiveDisputeRollupRule>) {
    fun validate(item: AdaptiveDisputeRollup): MutableList<String> {
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

    fun requireValid(item: AdaptiveDisputeRollup) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AdaptiveDisputeRollup>): MutableMap<String, MutableList<String>> {
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

fun defaultAdaptiveDisputeRollupValidator(): AdaptiveDisputeRollupValidator {
    return AdaptiveDisputeRollupValidator(
        mutableListOf<AdaptiveDisputeRollupRule>(
            AdaptiveDisputeRollupCarrierLengthRule(86400),
            AdaptiveDisputeRollupCodeLengthRule(120),
            AdaptiveDisputeRollupRequiredTitleRule("adaptive dispute rollup needs an entry"),
            AdaptiveDisputeRollupThresholdRangeRule(2.2, 741.8),
            AdaptiveDisputeRollupTitleLengthRule(120),
            AdaptiveDisputeRollupCodePrefixRule("Q"),
        ),
    )
}
