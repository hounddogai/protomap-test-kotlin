package com.acme.transport.lastmile.proofofdelivery.group

/** A rule a proof of delivery group must pass. Check returns its problems, or none. */
interface ProofOfDeliveryGroupRule {
    fun check(item: ProofOfDeliveryGroup): MutableList<String>
}

data class ProofOfDeliveryGroupScoreRangeRule(var min: Double, var max: Double) : ProofOfDeliveryGroupRule {
    override fun check(item: ProofOfDeliveryGroup): MutableList<String> {
        val value = item.score
        if (value < min || value > max) {
            return mutableListOf<String>("score $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class ProofOfDeliveryGroupRouteCodeLengthRule(var maxLength: Int) : ProofOfDeliveryGroupRule {
    override fun check(item: ProofOfDeliveryGroup): MutableList<String> {
        val size = item.routeCode.length
        if (size > maxLength) {
            return mutableListOf<String>("route code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ProofOfDeliveryGroupCodeLengthRule(var maxLength: Int) : ProofOfDeliveryGroupRule {
    override fun check(item: ProofOfDeliveryGroup): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a proof of delivery group. */
data class ProofOfDeliveryGroupValidator(var rules: MutableList<ProofOfDeliveryGroupRule>) {
    fun validate(item: ProofOfDeliveryGroup): MutableList<String> {
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

    fun requireValid(item: ProofOfDeliveryGroup) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ProofOfDeliveryGroup>): MutableMap<String, MutableList<String>> {
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

fun defaultProofOfDeliveryGroupValidator(): ProofOfDeliveryGroupValidator {
    return ProofOfDeliveryGroupValidator(
        mutableListOf<ProofOfDeliveryGroupRule>(
            ProofOfDeliveryGroupScoreRangeRule(5.3, 3498.9),
            ProofOfDeliveryGroupRouteCodeLengthRule(32),
            ProofOfDeliveryGroupCodeLengthRule(3600),
        ),
    )
}
