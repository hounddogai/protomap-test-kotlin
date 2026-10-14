package com.acme.transport.lastmile.proofofdelivery.score

import com.acme.transport.kit.text.normalize

/** A rule a proof of delivery variance must pass. Check returns its problems, or none. */
interface ProofOfDeliveryVarianceRule {
    fun check(item: ProofOfDeliveryVariance): MutableList<String>
}

data class ProofOfDeliveryVarianceLaneLengthRule(var maxLength: Int) : ProofOfDeliveryVarianceRule {
    override fun check(item: ProofOfDeliveryVariance): MutableList<String> {
        val size = item.lane.length
        if (size > maxLength) {
            return mutableListOf<String>("lane is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ProofOfDeliveryVarianceRequiredLabelRule(var message: String) : ProofOfDeliveryVarianceRule {
    override fun check(item: ProofOfDeliveryVariance): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ProofOfDeliveryVarianceRequiredCodeRule(var message: String) : ProofOfDeliveryVarianceRule {
    override fun check(item: ProofOfDeliveryVariance): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ProofOfDeliveryVarianceAllowedLaneRule(
    var allowed: MutableList<String>,
) : ProofOfDeliveryVarianceRule {
    override fun check(item: ProofOfDeliveryVariance): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.lane))) {
            return mutableListOf<String>("lane ${item.lane} is too long")
        }
        return mutableListOf<String>()
    }
}

data class ProofOfDeliveryVarianceRequiredNameRule(var message: String) : ProofOfDeliveryVarianceRule {
    override fun check(item: ProofOfDeliveryVariance): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ProofOfDeliveryVarianceLabelLengthRule(var maxLength: Int) : ProofOfDeliveryVarianceRule {
    override fun check(item: ProofOfDeliveryVariance): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a proof of delivery variance. */
data class ProofOfDeliveryVarianceValidator(var rules: MutableList<ProofOfDeliveryVarianceRule>) {
    fun validate(item: ProofOfDeliveryVariance): MutableList<String> {
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

    fun requireValid(item: ProofOfDeliveryVariance) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ProofOfDeliveryVariance>): MutableMap<String, MutableList<String>> {
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

fun defaultProofOfDeliveryVarianceValidator(): ProofOfDeliveryVarianceValidator {
    return ProofOfDeliveryVarianceValidator(
        mutableListOf<ProofOfDeliveryVarianceRule>(
            ProofOfDeliveryVarianceLaneLengthRule(80),
            ProofOfDeliveryVarianceRequiredLabelRule("proof of delivery variance needs a value"),
            ProofOfDeliveryVarianceRequiredCodeRule("proof of delivery variance needs this field"),
            ProofOfDeliveryVarianceAllowedLaneRule(mutableListOf<String>()),
            ProofOfDeliveryVarianceRequiredNameRule("proof of delivery variance needs this field"),
            ProofOfDeliveryVarianceLabelLengthRule(1000),
        ),
    )
}
