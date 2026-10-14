package com.acme.transport.fleetops.leasereturn.draft

/** A rule a regional lease return policy must pass. Check returns its problems, or none. */
interface RegionalLeaseReturnPolicyRule {
    fun check(item: RegionalLeaseReturnPolicy): MutableList<String>
}

data class RegionalLeaseReturnPolicyScoreRangeRule(
    var min: Double,
    var max: Double,
) : RegionalLeaseReturnPolicyRule {
    override fun check(item: RegionalLeaseReturnPolicy): MutableList<String> {
        val value = item.score
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("score $value is below $min")
        }
        if (value > max) {
            problems.add("score $value is above $max")
        }
        return problems
    }
}

data class RegionalLeaseReturnPolicyCodeLengthRule(var maxLength: Int) : RegionalLeaseReturnPolicyRule {
    override fun check(item: RegionalLeaseReturnPolicy): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RegionalLeaseReturnPolicyCodePrefixRule(var prefix: String) : RegionalLeaseReturnPolicyRule {
    override fun check(item: RegionalLeaseReturnPolicy): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a regional lease return policy. */
data class RegionalLeaseReturnPolicyValidator(var rules: MutableList<RegionalLeaseReturnPolicyRule>) {
    fun validate(item: RegionalLeaseReturnPolicy): MutableList<String> {
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

    fun requireValid(item: RegionalLeaseReturnPolicy) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RegionalLeaseReturnPolicy>): MutableMap<String, MutableList<String>> {
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

fun defaultRegionalLeaseReturnPolicyValidator(): RegionalLeaseReturnPolicyValidator {
    return RegionalLeaseReturnPolicyValidator(
        mutableListOf<RegionalLeaseReturnPolicyRule>(
            RegionalLeaseReturnPolicyScoreRangeRule(8.8, 3418.0),
            RegionalLeaseReturnPolicyCodeLengthRule(16),
            RegionalLeaseReturnPolicyCodePrefixRule("REF"),
        ),
    )
}
