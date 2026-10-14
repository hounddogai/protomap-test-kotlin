package com.acme.transport.lastmile.proofofdelivery.allocation

import com.acme.transport.kit.text.normalize

/** A rule a global proof of delivery allocation must pass. Check returns its problems, or none. */
interface GlobalProofOfDeliveryAllocationRule {
    fun check(item: GlobalProofOfDeliveryAllocation): MutableList<String>
}

data class GlobalProofOfDeliveryAllocationMinimumRangeRule(
    var min: Double,
    var max: Double,
) : GlobalProofOfDeliveryAllocationRule {
    override fun check(item: GlobalProofOfDeliveryAllocation): MutableList<String> {
        val value = item.minimum
        if (value < min || value > max) {
            return mutableListOf<String>("minimum $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class GlobalProofOfDeliveryAllocationAllowedCodeRule(
    var allowed: MutableList<String>,
) : GlobalProofOfDeliveryAllocationRule {
    override fun check(item: GlobalProofOfDeliveryAllocation): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is out of range")
        }
        return mutableListOf<String>()
    }
}

data class GlobalProofOfDeliveryAllocationChannelLengthRule(
    var maxLength: Int,
) : GlobalProofOfDeliveryAllocationRule {
    override fun check(item: GlobalProofOfDeliveryAllocation): MutableList<String> {
        val size = item.channel.length
        if (size > maxLength) {
            return mutableListOf<String>("channel is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class GlobalProofOfDeliveryAllocationRequiredNameRule(
    var message: String,
) : GlobalProofOfDeliveryAllocationRule {
    override fun check(item: GlobalProofOfDeliveryAllocation): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class GlobalProofOfDeliveryAllocationCodePrefixRule(
    var prefix: String,
) : GlobalProofOfDeliveryAllocationRule {
    override fun check(item: GlobalProofOfDeliveryAllocation): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a global proof of delivery allocation. */
data class GlobalProofOfDeliveryAllocationValidator(
    var rules: MutableList<GlobalProofOfDeliveryAllocationRule>,
) {
    fun validate(item: GlobalProofOfDeliveryAllocation): MutableList<String> {
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

    fun requireValid(item: GlobalProofOfDeliveryAllocation) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<GlobalProofOfDeliveryAllocation>): MutableMap<String, MutableList<String>> {
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

fun defaultGlobalProofOfDeliveryAllocationValidator(): GlobalProofOfDeliveryAllocationValidator {
    return GlobalProofOfDeliveryAllocationValidator(
        mutableListOf<GlobalProofOfDeliveryAllocationRule>(
            GlobalProofOfDeliveryAllocationMinimumRangeRule(1.2, 3479.3),
            GlobalProofOfDeliveryAllocationAllowedCodeRule(mutableListOf<String>("seasonal")),
            GlobalProofOfDeliveryAllocationChannelLengthRule(120),
            GlobalProofOfDeliveryAllocationRequiredNameRule(
                "global proof of delivery allocation needs this field",
            ),
            GlobalProofOfDeliveryAllocationCodePrefixRule("OPS"),
        ),
    )
}
