package com.acme.transport.lastmile.proofofdelivery.review

/** A rule a proof of delivery cycle must pass. Check returns its problems, or none. */
interface ProofOfDeliveryCycleRule {
    fun check(item: ProofOfDeliveryCycle): MutableList<String>
}

data class ProofOfDeliveryCycleRequiredNotesRule(var message: String) : ProofOfDeliveryCycleRule {
    override fun check(item: ProofOfDeliveryCycle): MutableList<String> {
        if (item.notes.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ProofOfDeliveryCycleAmountRangeRule(var min: Double, var max: Double) : ProofOfDeliveryCycleRule {
    override fun check(item: ProofOfDeliveryCycle): MutableList<String> {
        val value = item.amount
        if (value < min || value > max) {
            return mutableListOf<String>("amount $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class ProofOfDeliveryCycleCodeLengthRule(var maxLength: Int) : ProofOfDeliveryCycleRule {
    override fun check(item: ProofOfDeliveryCycle): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a proof of delivery cycle. */
data class ProofOfDeliveryCycleValidator(var rules: MutableList<ProofOfDeliveryCycleRule>) {
    fun validate(item: ProofOfDeliveryCycle): MutableList<String> {
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

    fun requireValid(item: ProofOfDeliveryCycle) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ProofOfDeliveryCycle>): MutableMap<String, MutableList<String>> {
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

fun defaultProofOfDeliveryCycleValidator(): ProofOfDeliveryCycleValidator {
    return ProofOfDeliveryCycleValidator(
        mutableListOf<ProofOfDeliveryCycleRule>(
            ProofOfDeliveryCycleRequiredNotesRule("proof of delivery cycle needs an entry"),
            ProofOfDeliveryCycleAmountRangeRule(5.5, 3667.7),
            ProofOfDeliveryCycleCodeLengthRule(1000),
        ),
    )
}
