package com.acme.transport.freightpay.dispute.disputepolicy

/** A rule a capped dispute policy must pass. Check returns its problems, or none. */
interface CappedDisputePolicyRule {
    fun check(item: CappedDisputePolicy): MutableList<String>
}

data class CappedDisputePolicyLocaleLengthRule(var maxLength: Int) : CappedDisputePolicyRule {
    override fun check(item: CappedDisputePolicy): MutableList<String> {
        val size = item.locale.length
        if (size > maxLength) {
            return mutableListOf<String>("locale is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CappedDisputePolicyRequiredLocaleRule(var message: String) : CappedDisputePolicyRule {
    override fun check(item: CappedDisputePolicy): MutableList<String> {
        if (item.locale.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CappedDisputePolicyOwnerIdLengthRule(var maxLength: Int) : CappedDisputePolicyRule {
    override fun check(item: CappedDisputePolicy): MutableList<String> {
        val size = item.ownerId.length
        if (size > maxLength) {
            return mutableListOf<String>("owner id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CappedDisputePolicyGraceDaysRangeRule(var min: Int, var max: Int) : CappedDisputePolicyRule {
    override fun check(item: CappedDisputePolicy): MutableList<String> {
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

data class CappedDisputePolicyLimitRangeRule(var min: Int, var max: Int) : CappedDisputePolicyRule {
    override fun check(item: CappedDisputePolicy): MutableList<String> {
        val value = item.limit
        if (value < min || value > max) {
            return mutableListOf<String>("limit $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class CappedDisputePolicyRequiredCodeRule(var message: String) : CappedDisputePolicyRule {
    override fun check(item: CappedDisputePolicy): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a capped dispute policy. */
data class CappedDisputePolicyValidator(var rules: MutableList<CappedDisputePolicyRule>) {
    fun validate(item: CappedDisputePolicy): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: CappedDisputePolicy) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CappedDisputePolicy>): MutableMap<String, MutableList<String>> {
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

fun defaultCappedDisputePolicyValidator(): CappedDisputePolicyValidator {
    return CappedDisputePolicyValidator(
        mutableListOf<CappedDisputePolicyRule>(
            CappedDisputePolicyLocaleLengthRule(86400),
            CappedDisputePolicyRequiredLocaleRule("capped dispute policy needs a value"),
            CappedDisputePolicyOwnerIdLengthRule(86400),
            CappedDisputePolicyGraceDaysRangeRule(16, 32),
            CappedDisputePolicyLimitRangeRule(32, 80),
            CappedDisputePolicyRequiredCodeRule("capped dispute policy needs this field"),
        ),
    )
}
