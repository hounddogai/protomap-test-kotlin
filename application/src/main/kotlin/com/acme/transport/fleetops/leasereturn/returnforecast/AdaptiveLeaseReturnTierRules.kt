package com.acme.transport.fleetops.leasereturn.returnforecast

/** A rule a adaptive lease return tier must pass. Check returns its problems, or none. */
interface AdaptiveLeaseReturnTierRule {
    fun check(item: AdaptiveLeaseReturnTier): MutableList<String>
}

data class AdaptiveLeaseReturnTierLabelLengthRule(var maxLength: Int) : AdaptiveLeaseReturnTierRule {
    override fun check(item: AdaptiveLeaseReturnTier): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveLeaseReturnTierCodeLengthRule(var maxLength: Int) : AdaptiveLeaseReturnTierRule {
    override fun check(item: AdaptiveLeaseReturnTier): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveLeaseReturnTierGraceDaysRangeRule(var min: Int, var max: Int) : AdaptiveLeaseReturnTierRule {
    override fun check(item: AdaptiveLeaseReturnTier): MutableList<String> {
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

data class AdaptiveLeaseReturnTierRequiredCodeRule(var message: String) : AdaptiveLeaseReturnTierRule {
    override fun check(item: AdaptiveLeaseReturnTier): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveLeaseReturnTierRequiredTitleRule(var message: String) : AdaptiveLeaseReturnTierRule {
    override fun check(item: AdaptiveLeaseReturnTier): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveLeaseReturnTierReviewerLengthRule(var maxLength: Int) : AdaptiveLeaseReturnTierRule {
    override fun check(item: AdaptiveLeaseReturnTier): MutableList<String> {
        val size = item.reviewer.length
        if (size > maxLength) {
            return mutableListOf<String>("reviewer is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a adaptive lease return tier. */
data class AdaptiveLeaseReturnTierValidator(var rules: MutableList<AdaptiveLeaseReturnTierRule>) {
    fun validate(item: AdaptiveLeaseReturnTier): MutableList<String> {
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

    fun requireValid(item: AdaptiveLeaseReturnTier) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AdaptiveLeaseReturnTier>): MutableMap<String, MutableList<String>> {
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

fun defaultAdaptiveLeaseReturnTierValidator(): AdaptiveLeaseReturnTierValidator {
    return AdaptiveLeaseReturnTierValidator(
        mutableListOf<AdaptiveLeaseReturnTierRule>(
            AdaptiveLeaseReturnTierLabelLengthRule(80),
            AdaptiveLeaseReturnTierCodeLengthRule(120),
            AdaptiveLeaseReturnTierGraceDaysRangeRule(64, 120),
            AdaptiveLeaseReturnTierRequiredCodeRule("adaptive lease return tier needs an entry"),
            AdaptiveLeaseReturnTierRequiredTitleRule("adaptive lease return tier needs a value"),
            AdaptiveLeaseReturnTierReviewerLengthRule(86400),
        ),
    )
}
