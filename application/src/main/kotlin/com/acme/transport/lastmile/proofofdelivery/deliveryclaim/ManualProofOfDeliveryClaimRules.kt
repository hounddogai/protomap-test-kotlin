package com.acme.transport.lastmile.proofofdelivery.deliveryclaim

import com.acme.transport.kit.text.normalize

/** A rule a manual proof of delivery claim must pass. Check returns its problems, or none. */
interface ManualProofOfDeliveryClaimRule {
    fun check(item: ManualProofOfDeliveryClaim): MutableList<String>
}

data class ManualProofOfDeliveryClaimRequiredDepotRule(var message: String) : ManualProofOfDeliveryClaimRule {
    override fun check(item: ManualProofOfDeliveryClaim): MutableList<String> {
        if (item.depot.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ManualProofOfDeliveryClaimCodePrefixRule(var prefix: String) : ManualProofOfDeliveryClaimRule {
    override fun check(item: ManualProofOfDeliveryClaim): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class ManualProofOfDeliveryClaimAllowedDepotRule(
    var allowed: MutableList<String>,
) : ManualProofOfDeliveryClaimRule {
    override fun check(item: ManualProofOfDeliveryClaim): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.depot))) {
            return mutableListOf<String>("depot ${item.depot} is inconsistent")
        }
        return mutableListOf<String>()
    }
}

data class ManualProofOfDeliveryClaimRequiredLabelRule(var message: String) : ManualProofOfDeliveryClaimRule {
    override fun check(item: ManualProofOfDeliveryClaim): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a manual proof of delivery claim. */
data class ManualProofOfDeliveryClaimValidator(var rules: MutableList<ManualProofOfDeliveryClaimRule>) {
    fun validate(item: ManualProofOfDeliveryClaim): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ManualProofOfDeliveryClaim) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ManualProofOfDeliveryClaim>): MutableMap<String, MutableList<String>> {
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

fun defaultManualProofOfDeliveryClaimValidator(): ManualProofOfDeliveryClaimValidator {
    return ManualProofOfDeliveryClaimValidator(
        mutableListOf<ManualProofOfDeliveryClaimRule>(
            ManualProofOfDeliveryClaimRequiredDepotRule("manual proof of delivery claim needs a value"),
            ManualProofOfDeliveryClaimCodePrefixRule("REF"),
            ManualProofOfDeliveryClaimAllowedDepotRule(mutableListOf<String>("default")),
            ManualProofOfDeliveryClaimRequiredLabelRule("manual proof of delivery claim needs an entry"),
        ),
    )
}
