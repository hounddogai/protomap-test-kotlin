package com.acme.transport.lastmile.proofofdelivery.entry

import com.acme.transport.kit.text.normalize

/** A rule a proof of delivery agreement must pass. Check returns its problems, or none. */
interface ProofOfDeliveryAgreementRule {
    fun check(item: ProofOfDeliveryAgreement): MutableList<String>
}

data class ProofOfDeliveryAgreementNameLengthRule(var maxLength: Int) : ProofOfDeliveryAgreementRule {
    override fun check(item: ProofOfDeliveryAgreement): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ProofOfDeliveryAgreementWindowDaysRangeRule(
    var min: Int,
    var max: Int,
) : ProofOfDeliveryAgreementRule {
    override fun check(item: ProofOfDeliveryAgreement): MutableList<String> {
        val value = item.windowDays
        if (value < min || value > max) {
            return mutableListOf<String>("window days $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class ProofOfDeliveryAgreementAllowedCodeRule(
    var allowed: MutableList<String>,
) : ProofOfDeliveryAgreementRule {
    override fun check(item: ProofOfDeliveryAgreement): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is duplicated")
        }
        return mutableListOf<String>()
    }
}

data class ProofOfDeliveryAgreementCodePrefixRule(var prefix: String) : ProofOfDeliveryAgreementRule {
    override fun check(item: ProofOfDeliveryAgreement): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a proof of delivery agreement. */
data class ProofOfDeliveryAgreementValidator(var rules: MutableList<ProofOfDeliveryAgreementRule>) {
    fun validate(item: ProofOfDeliveryAgreement): MutableList<String> {
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

    fun requireValid(item: ProofOfDeliveryAgreement) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ProofOfDeliveryAgreement>): MutableMap<String, MutableList<String>> {
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

fun defaultProofOfDeliveryAgreementValidator(): ProofOfDeliveryAgreementValidator {
    return ProofOfDeliveryAgreementValidator(
        mutableListOf<ProofOfDeliveryAgreementRule>(
            ProofOfDeliveryAgreementNameLengthRule(3600),
            ProofOfDeliveryAgreementWindowDaysRangeRule(80, 3600),
            ProofOfDeliveryAgreementAllowedCodeRule(mutableListOf<String>("strict", "primary", "batched")),
            ProofOfDeliveryAgreementCodePrefixRule("OPS"),
        ),
    )
}
