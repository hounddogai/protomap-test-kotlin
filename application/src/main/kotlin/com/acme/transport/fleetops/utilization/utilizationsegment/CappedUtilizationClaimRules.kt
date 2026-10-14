package com.acme.transport.fleetops.utilization.utilizationsegment

/** A rule a capped utilization claim must pass. Check returns its problems, or none. */
interface CappedUtilizationClaimRule {
    fun check(item: CappedUtilizationClaim): MutableList<String>
}

data class CappedUtilizationClaimCarrierLengthRule(var maxLength: Int) : CappedUtilizationClaimRule {
    override fun check(item: CappedUtilizationClaim): MutableList<String> {
        val size = item.carrier.length
        if (size > maxLength) {
            return mutableListOf<String>("carrier is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CappedUtilizationClaimCodePrefixRule(var prefix: String) : CappedUtilizationClaimRule {
    override fun check(item: CappedUtilizationClaim): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class CappedUtilizationClaimCodeLengthRule(var maxLength: Int) : CappedUtilizationClaimRule {
    override fun check(item: CappedUtilizationClaim): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CappedUtilizationClaimMarginRangeRule(
    var min: Double,
    var max: Double,
) : CappedUtilizationClaimRule {
    override fun check(item: CappedUtilizationClaim): MutableList<String> {
        val value = item.margin
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("margin $value is below $min")
        }
        if (value > max) {
            problems.add("margin $value is above $max")
        }
        return problems
    }
}

data class CappedUtilizationClaimRateRangeRule(var min: Double, var max: Double) : CappedUtilizationClaimRule {
    override fun check(item: CappedUtilizationClaim): MutableList<String> {
        val value = item.rate
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("rate $value is below $min")
        }
        if (value > max) {
            problems.add("rate $value is above $max")
        }
        return problems
    }
}

data class CappedUtilizationClaimTitleLengthRule(var maxLength: Int) : CappedUtilizationClaimRule {
    override fun check(item: CappedUtilizationClaim): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a capped utilization claim. */
data class CappedUtilizationClaimValidator(var rules: MutableList<CappedUtilizationClaimRule>) {
    fun validate(item: CappedUtilizationClaim): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: CappedUtilizationClaim) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CappedUtilizationClaim>): MutableMap<String, MutableList<String>> {
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

fun defaultCappedUtilizationClaimValidator(): CappedUtilizationClaimValidator {
    return CappedUtilizationClaimValidator(
        mutableListOf<CappedUtilizationClaimRule>(
            CappedUtilizationClaimCarrierLengthRule(1000),
            CappedUtilizationClaimCodePrefixRule("X"),
            CappedUtilizationClaimCodeLengthRule(16),
            CappedUtilizationClaimMarginRangeRule(4.4, 3162.5),
            CappedUtilizationClaimRateRangeRule(5.5, 1182.5),
            CappedUtilizationClaimTitleLengthRule(86400),
        ),
    )
}
