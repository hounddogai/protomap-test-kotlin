package com.acme.transport.lastmile.proofofdelivery.exception

/** A rule a proof of delivery exception must pass. Check returns its problems, or none. */
interface ProofOfDeliveryExceptionRule {
    fun check(item: ProofOfDeliveryException): MutableList<String>
}

data class ProofOfDeliveryExceptionFloorValueRangeRule(
    var min: Double,
    var max: Double,
) : ProofOfDeliveryExceptionRule {
    override fun check(item: ProofOfDeliveryException): MutableList<String> {
        val value = item.floorValue
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("floor value $value is below $min")
        }
        if (value > max) {
            problems.add("floor value $value is above $max")
        }
        return problems
    }
}

data class ProofOfDeliveryExceptionExternalRefLengthRule(var maxLength: Int) : ProofOfDeliveryExceptionRule {
    override fun check(item: ProofOfDeliveryException): MutableList<String> {
        val size = item.externalRef.length
        if (size > maxLength) {
            return mutableListOf<String>("external ref is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ProofOfDeliveryExceptionRequiredChecksumRule(var message: String) : ProofOfDeliveryExceptionRule {
    override fun check(item: ProofOfDeliveryException): MutableList<String> {
        if (item.checksum.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ProofOfDeliveryExceptionNameLengthRule(var maxLength: Int) : ProofOfDeliveryExceptionRule {
    override fun check(item: ProofOfDeliveryException): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a proof of delivery exception. */
data class ProofOfDeliveryExceptionValidator(var rules: MutableList<ProofOfDeliveryExceptionRule>) {
    fun validate(item: ProofOfDeliveryException): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ProofOfDeliveryException) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ProofOfDeliveryException>): MutableMap<String, MutableList<String>> {
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

fun defaultProofOfDeliveryExceptionValidator(): ProofOfDeliveryExceptionValidator {
    return ProofOfDeliveryExceptionValidator(
        mutableListOf<ProofOfDeliveryExceptionRule>(
            ProofOfDeliveryExceptionFloorValueRangeRule(1.5, 3804.7),
            ProofOfDeliveryExceptionExternalRefLengthRule(16),
            ProofOfDeliveryExceptionRequiredChecksumRule("proof of delivery exception needs this field"),
            ProofOfDeliveryExceptionNameLengthRule(120),
        ),
    )
}
