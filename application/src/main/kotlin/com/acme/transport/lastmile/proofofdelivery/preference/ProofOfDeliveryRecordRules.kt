package com.acme.transport.lastmile.proofofdelivery.preference

import com.acme.transport.kit.text.normalize

/** A rule a proof of delivery record must pass. Check returns its problems, or none. */
interface ProofOfDeliveryRecordRule {
    fun check(item: ProofOfDeliveryRecord): MutableList<String>
}

data class ProofOfDeliveryRecordAllowedContactEmailRule(
    var allowed: MutableList<String>,
) : ProofOfDeliveryRecordRule {
    override fun check(item: ProofOfDeliveryRecord): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.contactEmail))) {
            return mutableListOf<String>("contact email ${item.contactEmail} is above maximum")
        }
        return mutableListOf<String>()
    }
}

data class ProofOfDeliveryRecordCodeLengthRule(var maxLength: Int) : ProofOfDeliveryRecordRule {
    override fun check(item: ProofOfDeliveryRecord): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ProofOfDeliveryRecordRequiredContactEmailRule(var message: String) : ProofOfDeliveryRecordRule {
    override fun check(item: ProofOfDeliveryRecord): MutableList<String> {
        if (item.contactEmail.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ProofOfDeliveryRecordPriorityRangeRule(var min: Int, var max: Int) : ProofOfDeliveryRecordRule {
    override fun check(item: ProofOfDeliveryRecord): MutableList<String> {
        val value = item.priority
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("priority $value is below $min")
        }
        if (value > max) {
            problems.add("priority $value is above $max")
        }
        return problems
    }
}

data class ProofOfDeliveryRecordRevisionRangeRule(var min: Int, var max: Int) : ProofOfDeliveryRecordRule {
    override fun check(item: ProofOfDeliveryRecord): MutableList<String> {
        val value = item.revision
        if (value < min || value > max) {
            return mutableListOf<String>("revision $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a proof of delivery record. */
data class ProofOfDeliveryRecordValidator(var rules: MutableList<ProofOfDeliveryRecordRule>) {
    fun validate(item: ProofOfDeliveryRecord): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ProofOfDeliveryRecord) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ProofOfDeliveryRecord>): MutableMap<String, MutableList<String>> {
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

fun defaultProofOfDeliveryRecordValidator(): ProofOfDeliveryRecordValidator {
    return ProofOfDeliveryRecordValidator(
        mutableListOf<ProofOfDeliveryRecordRule>(
            ProofOfDeliveryRecordAllowedContactEmailRule(mutableListOf<String>("capped")),
            ProofOfDeliveryRecordCodeLengthRule(80),
            ProofOfDeliveryRecordRequiredContactEmailRule("proof of delivery record needs a value"),
            ProofOfDeliveryRecordPriorityRangeRule(32, 120),
            ProofOfDeliveryRecordRevisionRangeRule(16, 255),
        ),
    )
}
