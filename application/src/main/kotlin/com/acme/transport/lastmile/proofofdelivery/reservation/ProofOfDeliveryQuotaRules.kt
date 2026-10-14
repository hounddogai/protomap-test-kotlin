package com.acme.transport.lastmile.proofofdelivery.reservation

import com.acme.transport.kit.text.normalize

/** A rule a proof of delivery quota must pass. Check returns its problems, or none. */
interface ProofOfDeliveryQuotaRule {
    fun check(item: ProofOfDeliveryQuota): MutableList<String>
}

data class ProofOfDeliveryQuotaDistanceKmRangeRule(
    var min: Double,
    var max: Double,
) : ProofOfDeliveryQuotaRule {
    override fun check(item: ProofOfDeliveryQuota): MutableList<String> {
        val value = item.distanceKm
        if (value < min || value > max) {
            return mutableListOf<String>("distance km $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class ProofOfDeliveryQuotaAllowedNameRule(var allowed: MutableList<String>) : ProofOfDeliveryQuotaRule {
    override fun check(item: ProofOfDeliveryQuota): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.name))) {
            return mutableListOf<String>("name ${item.name} is out of range")
        }
        return mutableListOf<String>()
    }
}

data class ProofOfDeliveryQuotaRequiredCodeRule(var message: String) : ProofOfDeliveryQuotaRule {
    override fun check(item: ProofOfDeliveryQuota): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a proof of delivery quota. */
data class ProofOfDeliveryQuotaValidator(var rules: MutableList<ProofOfDeliveryQuotaRule>) {
    fun validate(item: ProofOfDeliveryQuota): MutableList<String> {
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

    fun requireValid(item: ProofOfDeliveryQuota) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ProofOfDeliveryQuota>): MutableMap<String, MutableList<String>> {
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

fun defaultProofOfDeliveryQuotaValidator(): ProofOfDeliveryQuotaValidator {
    return ProofOfDeliveryQuotaValidator(
        mutableListOf<ProofOfDeliveryQuotaRule>(
            ProofOfDeliveryQuotaDistanceKmRangeRule(2.9, 1505.6),
            ProofOfDeliveryQuotaAllowedNameRule(mutableListOf<String>()),
            ProofOfDeliveryQuotaRequiredCodeRule("proof of delivery quota needs an entry"),
        ),
    )
}
