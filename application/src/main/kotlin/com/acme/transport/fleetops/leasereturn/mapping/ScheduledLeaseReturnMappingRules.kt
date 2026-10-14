package com.acme.transport.fleetops.leasereturn.mapping

import com.acme.transport.kit.text.normalize

/** A rule a scheduled lease return mapping must pass. Check returns its problems, or none. */
interface ScheduledLeaseReturnMappingRule {
    fun check(item: ScheduledLeaseReturnMapping): MutableList<String>
}

data class ScheduledLeaseReturnMappingAllowedDepotRule(
    var allowed: MutableList<String>,
) : ScheduledLeaseReturnMappingRule {
    override fun check(item: ScheduledLeaseReturnMapping): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.depot))) {
            return mutableListOf<String>("depot ${item.depot} is inconsistent")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledLeaseReturnMappingCodePrefixRule(var prefix: String) : ScheduledLeaseReturnMappingRule {
    override fun check(item: ScheduledLeaseReturnMapping): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledLeaseReturnMappingSummaryLengthRule(var maxLength: Int) : ScheduledLeaseReturnMappingRule {
    override fun check(item: ScheduledLeaseReturnMapping): MutableList<String> {
        val size = item.summary.length
        if (size > maxLength) {
            return mutableListOf<String>("summary is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledLeaseReturnMappingAmountRangeRule(
    var min: Double,
    var max: Double,
) : ScheduledLeaseReturnMappingRule {
    override fun check(item: ScheduledLeaseReturnMapping): MutableList<String> {
        val value = item.amount
        if (value < min || value > max) {
            return mutableListOf<String>("amount $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a scheduled lease return mapping. */
data class ScheduledLeaseReturnMappingValidator(var rules: MutableList<ScheduledLeaseReturnMappingRule>) {
    fun validate(item: ScheduledLeaseReturnMapping): MutableList<String> {
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

    fun requireValid(item: ScheduledLeaseReturnMapping) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ScheduledLeaseReturnMapping>): MutableMap<String, MutableList<String>> {
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

fun defaultScheduledLeaseReturnMappingValidator(): ScheduledLeaseReturnMappingValidator {
    return ScheduledLeaseReturnMappingValidator(
        mutableListOf<ScheduledLeaseReturnMappingRule>(
            ScheduledLeaseReturnMappingAllowedDepotRule(mutableListOf<String>()),
            ScheduledLeaseReturnMappingCodePrefixRule("ACM"),
            ScheduledLeaseReturnMappingSummaryLengthRule(32),
            ScheduledLeaseReturnMappingAmountRangeRule(6.8, 1429.1),
        ),
    )
}
