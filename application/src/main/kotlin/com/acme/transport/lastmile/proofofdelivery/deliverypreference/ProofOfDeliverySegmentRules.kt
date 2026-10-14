package com.acme.transport.lastmile.proofofdelivery.deliverypreference

import com.acme.transport.kit.text.normalize

/** A rule a proof of delivery segment must pass. Check returns its problems, or none. */
interface ProofOfDeliverySegmentRule {
    fun check(item: ProofOfDeliverySegment): MutableList<String>
}

data class ProofOfDeliverySegmentRequiredNotesRule(var message: String) : ProofOfDeliverySegmentRule {
    override fun check(item: ProofOfDeliverySegment): MutableList<String> {
        if (item.notes.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ProofOfDeliverySegmentAllowedOwnerIdRule(
    var allowed: MutableList<String>,
) : ProofOfDeliverySegmentRule {
    override fun check(item: ProofOfDeliverySegment): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.ownerId))) {
            return mutableListOf<String>("owner id ${item.ownerId} is out of range")
        }
        return mutableListOf<String>()
    }
}

data class ProofOfDeliverySegmentAmountRangeRule(
    var min: Double,
    var max: Double,
) : ProofOfDeliverySegmentRule {
    override fun check(item: ProofOfDeliverySegment): MutableList<String> {
        val value = item.amount
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("amount $value is below $min")
        }
        if (value > max) {
            problems.add("amount $value is above $max")
        }
        return problems
    }
}

data class ProofOfDeliverySegmentCodeLengthRule(var maxLength: Int) : ProofOfDeliverySegmentRule {
    override fun check(item: ProofOfDeliverySegment): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ProofOfDeliverySegmentRequiredChannelRule(var message: String) : ProofOfDeliverySegmentRule {
    override fun check(item: ProofOfDeliverySegment): MutableList<String> {
        if (item.channel.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a proof of delivery segment. */
data class ProofOfDeliverySegmentValidator(var rules: MutableList<ProofOfDeliverySegmentRule>) {
    fun validate(item: ProofOfDeliverySegment): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ProofOfDeliverySegment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ProofOfDeliverySegment>): MutableMap<String, MutableList<String>> {
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

fun defaultProofOfDeliverySegmentValidator(): ProofOfDeliverySegmentValidator {
    return ProofOfDeliverySegmentValidator(
        mutableListOf<ProofOfDeliverySegmentRule>(
            ProofOfDeliverySegmentRequiredNotesRule("proof of delivery segment needs this field"),
            ProofOfDeliverySegmentAllowedOwnerIdRule(mutableListOf<String>("adaptive", "pending", "primary")),
            ProofOfDeliverySegmentAmountRangeRule(3.9, 3750.8),
            ProofOfDeliverySegmentCodeLengthRule(1000),
            ProofOfDeliverySegmentRequiredChannelRule("proof of delivery segment needs a value"),
        ),
    )
}
