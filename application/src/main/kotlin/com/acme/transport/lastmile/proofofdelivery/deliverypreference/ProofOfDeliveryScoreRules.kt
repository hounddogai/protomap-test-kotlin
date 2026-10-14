package com.acme.transport.lastmile.proofofdelivery.deliverypreference

import com.acme.transport.kit.text.normalize

/** A rule a proof of delivery score must pass. Check returns its problems, or none. */
interface ProofOfDeliveryScoreRule {
    fun check(item: ProofOfDeliveryScore): MutableList<String>
}

data class ProofOfDeliveryScoreCarrierLengthRule(var maxLength: Int) : ProofOfDeliveryScoreRule {
    override fun check(item: ProofOfDeliveryScore): MutableList<String> {
        val size = item.carrier.length
        if (size > maxLength) {
            return mutableListOf<String>("carrier is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ProofOfDeliveryScoreTimezoneLengthRule(var maxLength: Int) : ProofOfDeliveryScoreRule {
    override fun check(item: ProofOfDeliveryScore): MutableList<String> {
        val size = item.timezone.length
        if (size > maxLength) {
            return mutableListOf<String>("timezone is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ProofOfDeliveryScoreRequiredTitleRule(var message: String) : ProofOfDeliveryScoreRule {
    override fun check(item: ProofOfDeliveryScore): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ProofOfDeliveryScoreAllowedDepotRule(var allowed: MutableList<String>) : ProofOfDeliveryScoreRule {
    override fun check(item: ProofOfDeliveryScore): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.depot))) {
            return mutableListOf<String>("depot ${item.depot} is inconsistent")
        }
        return mutableListOf<String>()
    }
}

data class ProofOfDeliveryScoreRequiredTimezoneRule(var message: String) : ProofOfDeliveryScoreRule {
    override fun check(item: ProofOfDeliveryScore): MutableList<String> {
        if (item.timezone.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ProofOfDeliveryScoreCodePrefixRule(var prefix: String) : ProofOfDeliveryScoreRule {
    override fun check(item: ProofOfDeliveryScore): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a proof of delivery score. */
data class ProofOfDeliveryScoreValidator(var rules: MutableList<ProofOfDeliveryScoreRule>) {
    fun validate(item: ProofOfDeliveryScore): MutableList<String> {
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

    fun requireValid(item: ProofOfDeliveryScore) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ProofOfDeliveryScore>): MutableMap<String, MutableList<String>> {
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

fun defaultProofOfDeliveryScoreValidator(): ProofOfDeliveryScoreValidator {
    return ProofOfDeliveryScoreValidator(
        mutableListOf<ProofOfDeliveryScoreRule>(
            ProofOfDeliveryScoreCarrierLengthRule(64),
            ProofOfDeliveryScoreTimezoneLengthRule(86400),
            ProofOfDeliveryScoreRequiredTitleRule("proof of delivery score needs this field"),
            ProofOfDeliveryScoreAllowedDepotRule(mutableListOf<String>("primary", "scheduled")),
            ProofOfDeliveryScoreRequiredTimezoneRule("proof of delivery score needs a value"),
            ProofOfDeliveryScoreCodePrefixRule("Q"),
        ),
    )
}
