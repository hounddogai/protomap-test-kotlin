package com.acme.transport.lastmile.proofofdelivery.review

/** A rule a proof of delivery policy must pass. Check returns its problems, or none. */
interface ProofOfDeliveryPolicyRule {
    fun check(item: ProofOfDeliveryPolicy): MutableList<String>
}

data class ProofOfDeliveryPolicyNameLengthRule(var maxLength: Int) : ProofOfDeliveryPolicyRule {
    override fun check(item: ProofOfDeliveryPolicy): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ProofOfDeliveryPolicyCodePrefixRule(var prefix: String) : ProofOfDeliveryPolicyRule {
    override fun check(item: ProofOfDeliveryPolicy): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class ProofOfDeliveryPolicyCodeLengthRule(var maxLength: Int) : ProofOfDeliveryPolicyRule {
    override fun check(item: ProofOfDeliveryPolicy): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ProofOfDeliveryPolicyChecksumLengthRule(var maxLength: Int) : ProofOfDeliveryPolicyRule {
    override fun check(item: ProofOfDeliveryPolicy): MutableList<String> {
        val size = item.checksum.length
        if (size > maxLength) {
            return mutableListOf<String>("checksum is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ProofOfDeliveryPolicyFuelLitersRangeRule(
    var min: Double,
    var max: Double,
) : ProofOfDeliveryPolicyRule {
    override fun check(item: ProofOfDeliveryPolicy): MutableList<String> {
        val value = item.fuelLiters
        if (value < min || value > max) {
            return mutableListOf<String>("fuel liters $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a proof of delivery policy. */
data class ProofOfDeliveryPolicyValidator(var rules: MutableList<ProofOfDeliveryPolicyRule>) {
    fun validate(item: ProofOfDeliveryPolicy): MutableList<String> {
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

    fun requireValid(item: ProofOfDeliveryPolicy) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ProofOfDeliveryPolicy>): MutableMap<String, MutableList<String>> {
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

fun defaultProofOfDeliveryPolicyValidator(): ProofOfDeliveryPolicyValidator {
    return ProofOfDeliveryPolicyValidator(
        mutableListOf<ProofOfDeliveryPolicyRule>(
            ProofOfDeliveryPolicyNameLengthRule(16),
            ProofOfDeliveryPolicyCodePrefixRule("Q"),
            ProofOfDeliveryPolicyCodeLengthRule(80),
            ProofOfDeliveryPolicyChecksumLengthRule(80),
            ProofOfDeliveryPolicyFuelLitersRangeRule(1.2, 2835.3),
        ),
    )
}
