package com.acme.transport.lastmile.proofofdelivery.target

import com.acme.transport.kit.text.normalize

/** A rule a proof of delivery threshold must pass. Check returns its problems, or none. */
interface ProofOfDeliveryThresholdRule {
    fun check(item: ProofOfDeliveryThreshold): MutableList<String>
}

data class ProofOfDeliveryThresholdCodeLengthRule(var maxLength: Int) : ProofOfDeliveryThresholdRule {
    override fun check(item: ProofOfDeliveryThreshold): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ProofOfDeliveryThresholdVehicleIdLengthRule(var maxLength: Int) : ProofOfDeliveryThresholdRule {
    override fun check(item: ProofOfDeliveryThreshold): MutableList<String> {
        val size = item.vehicleId.length
        if (size > maxLength) {
            return mutableListOf<String>("vehicle id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ProofOfDeliveryThresholdCodePrefixRule(var prefix: String) : ProofOfDeliveryThresholdRule {
    override fun check(item: ProofOfDeliveryThreshold): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class ProofOfDeliveryThresholdAllowedVehicleIdRule(
    var allowed: MutableList<String>,
) : ProofOfDeliveryThresholdRule {
    override fun check(item: ProofOfDeliveryThreshold): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.vehicleId))) {
            return mutableListOf<String>("vehicle id ${item.vehicleId} is too long")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a proof of delivery threshold. */
data class ProofOfDeliveryThresholdValidator(var rules: MutableList<ProofOfDeliveryThresholdRule>) {
    fun validate(item: ProofOfDeliveryThreshold): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ProofOfDeliveryThreshold) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ProofOfDeliveryThreshold>): MutableMap<String, MutableList<String>> {
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

fun defaultProofOfDeliveryThresholdValidator(): ProofOfDeliveryThresholdValidator {
    return ProofOfDeliveryThresholdValidator(
        mutableListOf<ProofOfDeliveryThresholdRule>(
            ProofOfDeliveryThresholdCodeLengthRule(16),
            ProofOfDeliveryThresholdVehicleIdLengthRule(64),
            ProofOfDeliveryThresholdCodePrefixRule("X"),
            ProofOfDeliveryThresholdAllowedVehicleIdRule(mutableListOf<String>("default", "batched", "strict")),
        ),
    )
}
