package com.acme.transport.lastmile.proofofdelivery.deliveryreview

import com.acme.transport.kit.text.normalize

/** A rule a rolling proof of delivery mapping must pass. Check returns its problems, or none. */
interface RollingProofOfDeliveryMappingRule {
    fun check(item: RollingProofOfDeliveryMapping): MutableList<String>
}

data class RollingProofOfDeliveryMappingChecksumLengthRule(
    var maxLength: Int,
) : RollingProofOfDeliveryMappingRule {
    override fun check(item: RollingProofOfDeliveryMapping): MutableList<String> {
        val size = item.checksum.length
        if (size > maxLength) {
            return mutableListOf<String>("checksum is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RollingProofOfDeliveryMappingCodePrefixRule(var prefix: String) : RollingProofOfDeliveryMappingRule {
    override fun check(item: RollingProofOfDeliveryMapping): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class RollingProofOfDeliveryMappingMinimumRangeRule(
    var min: Double,
    var max: Double,
) : RollingProofOfDeliveryMappingRule {
    override fun check(item: RollingProofOfDeliveryMapping): MutableList<String> {
        val value = item.minimum
        if (value < min || value > max) {
            return mutableListOf<String>("minimum $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class RollingProofOfDeliveryMappingAllowedChecksumRule(
    var allowed: MutableList<String>,
) : RollingProofOfDeliveryMappingRule {
    override fun check(item: RollingProofOfDeliveryMapping): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.checksum))) {
            return mutableListOf<String>("checksum ${item.checksum} is unknown")
        }
        return mutableListOf<String>()
    }
}

data class RollingProofOfDeliveryMappingRequiredChecksumRule(
    var message: String,
) : RollingProofOfDeliveryMappingRule {
    override fun check(item: RollingProofOfDeliveryMapping): MutableList<String> {
        if (item.checksum.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RollingProofOfDeliveryMappingCodeLengthRule(var maxLength: Int) : RollingProofOfDeliveryMappingRule {
    override fun check(item: RollingProofOfDeliveryMapping): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a rolling proof of delivery mapping. */
data class RollingProofOfDeliveryMappingValidator(var rules: MutableList<RollingProofOfDeliveryMappingRule>) {
    fun validate(item: RollingProofOfDeliveryMapping): MutableList<String> {
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

    fun requireValid(item: RollingProofOfDeliveryMapping) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RollingProofOfDeliveryMapping>): MutableMap<String, MutableList<String>> {
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

fun defaultRollingProofOfDeliveryMappingValidator(): RollingProofOfDeliveryMappingValidator {
    return RollingProofOfDeliveryMappingValidator(
        mutableListOf<RollingProofOfDeliveryMappingRule>(
            RollingProofOfDeliveryMappingChecksumLengthRule(64),
            RollingProofOfDeliveryMappingCodePrefixRule("REF"),
            RollingProofOfDeliveryMappingMinimumRangeRule(1.2, 2122.3),
            RollingProofOfDeliveryMappingAllowedChecksumRule(mutableListOf<String>("primary")),
            RollingProofOfDeliveryMappingRequiredChecksumRule(
                "rolling proof of delivery mapping needs this field",
            ),
            RollingProofOfDeliveryMappingCodeLengthRule(80),
        ),
    )
}
