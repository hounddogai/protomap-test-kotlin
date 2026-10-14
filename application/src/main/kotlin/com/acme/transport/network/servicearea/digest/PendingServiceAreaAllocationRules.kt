package com.acme.transport.network.servicearea.digest

import com.acme.transport.kit.text.normalize

/** A rule a pending service area allocation must pass. Check returns its problems, or none. */
interface PendingServiceAreaAllocationRule {
    fun check(item: PendingServiceAreaAllocation): MutableList<String>
}

data class PendingServiceAreaAllocationScoreRangeRule(
    var min: Double,
    var max: Double,
) : PendingServiceAreaAllocationRule {
    override fun check(item: PendingServiceAreaAllocation): MutableList<String> {
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

data class PendingServiceAreaAllocationCodeLengthRule(var maxLength: Int) : PendingServiceAreaAllocationRule {
    override fun check(item: PendingServiceAreaAllocation): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PendingServiceAreaAllocationAllowedPhoneRule(
    var allowed: MutableList<String>,
) : PendingServiceAreaAllocationRule {
    override fun check(item: PendingServiceAreaAllocation): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.phone))) {
            return mutableListOf<String>("phone ${item.phone} is inconsistent")
        }
        return mutableListOf<String>()
    }
}

data class PendingServiceAreaAllocationVersionRangeRule(
    var min: Int,
    var max: Int,
) : PendingServiceAreaAllocationRule {
    override fun check(item: PendingServiceAreaAllocation): MutableList<String> {
        val value = item.version
        if (value < min || value > max) {
            return mutableListOf<String>("version $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class PendingServiceAreaAllocationCodePrefixRule(var prefix: String) : PendingServiceAreaAllocationRule {
    override fun check(item: PendingServiceAreaAllocation): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a pending service area allocation. */
data class PendingServiceAreaAllocationValidator(var rules: MutableList<PendingServiceAreaAllocationRule>) {
    fun validate(item: PendingServiceAreaAllocation): MutableList<String> {
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

    fun requireValid(item: PendingServiceAreaAllocation) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PendingServiceAreaAllocation>): MutableMap<String, MutableList<String>> {
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

fun defaultPendingServiceAreaAllocationValidator(): PendingServiceAreaAllocationValidator {
    return PendingServiceAreaAllocationValidator(
        mutableListOf<PendingServiceAreaAllocationRule>(
            PendingServiceAreaAllocationScoreRangeRule(2.5, 4063.6),
            PendingServiceAreaAllocationCodeLengthRule(86400),
            PendingServiceAreaAllocationAllowedPhoneRule(mutableListOf<String>()),
            PendingServiceAreaAllocationVersionRangeRule(1000, 1000),
            PendingServiceAreaAllocationCodePrefixRule("X"),
        ),
    )
}
