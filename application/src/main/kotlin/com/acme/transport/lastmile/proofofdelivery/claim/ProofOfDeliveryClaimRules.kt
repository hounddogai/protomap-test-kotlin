package com.acme.transport.lastmile.proofofdelivery.claim

/** A rule a proof of delivery claim must pass. Check returns its problems, or none. */
interface ProofOfDeliveryClaimRule {
    fun check(item: ProofOfDeliveryClaim): MutableList<String>
}

data class ProofOfDeliveryClaimCodeLengthRule(var maxLength: Int) : ProofOfDeliveryClaimRule {
    override fun check(item: ProofOfDeliveryClaim): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ProofOfDeliveryClaimCodePrefixRule(var prefix: String) : ProofOfDeliveryClaimRule {
    override fun check(item: ProofOfDeliveryClaim): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class ProofOfDeliveryClaimNameLengthRule(var maxLength: Int) : ProofOfDeliveryClaimRule {
    override fun check(item: ProofOfDeliveryClaim): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ProofOfDeliveryClaimDescriptionLengthRule(var maxLength: Int) : ProofOfDeliveryClaimRule {
    override fun check(item: ProofOfDeliveryClaim): MutableList<String> {
        val size = item.description.length
        if (size > maxLength) {
            return mutableListOf<String>("description is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a proof of delivery claim. */
data class ProofOfDeliveryClaimValidator(var rules: MutableList<ProofOfDeliveryClaimRule>) {
    fun validate(item: ProofOfDeliveryClaim): MutableList<String> {
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

    fun requireValid(item: ProofOfDeliveryClaim) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ProofOfDeliveryClaim>): MutableMap<String, MutableList<String>> {
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

fun defaultProofOfDeliveryClaimValidator(): ProofOfDeliveryClaimValidator {
    return ProofOfDeliveryClaimValidator(
        mutableListOf<ProofOfDeliveryClaimRule>(
            ProofOfDeliveryClaimCodeLengthRule(1000),
            ProofOfDeliveryClaimCodePrefixRule("REF"),
            ProofOfDeliveryClaimNameLengthRule(80),
            ProofOfDeliveryClaimDescriptionLengthRule(86400),
        ),
    )
}
