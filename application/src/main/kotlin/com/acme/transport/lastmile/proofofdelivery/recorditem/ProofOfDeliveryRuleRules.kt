package com.acme.transport.lastmile.proofofdelivery.recorditem

/** A rule a proof of delivery rule must pass. Check returns its problems, or none. */
interface ProofOfDeliveryRuleRule {
    fun check(item: ProofOfDeliveryRule): MutableList<String>
}

data class ProofOfDeliveryRuleCeilingRangeRule(var min: Double, var max: Double) : ProofOfDeliveryRuleRule {
    override fun check(item: ProofOfDeliveryRule): MutableList<String> {
        val value = item.ceiling
        if (value < min || value > max) {
            return mutableListOf<String>("ceiling $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class ProofOfDeliveryRuleDurationMinutesRangeRule(var min: Int, var max: Int) : ProofOfDeliveryRuleRule {
    override fun check(item: ProofOfDeliveryRule): MutableList<String> {
        val value = item.durationMinutes
        if (value < min || value > max) {
            return mutableListOf<String>("duration minutes $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class ProofOfDeliveryRuleLabelLengthRule(var maxLength: Int) : ProofOfDeliveryRuleRule {
    override fun check(item: ProofOfDeliveryRule): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a proof of delivery rule. */
data class ProofOfDeliveryRuleValidator(var rules: MutableList<ProofOfDeliveryRuleRule>) {
    fun validate(item: ProofOfDeliveryRule): MutableList<String> {
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

    fun requireValid(item: ProofOfDeliveryRule) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ProofOfDeliveryRule>): MutableMap<String, MutableList<String>> {
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

fun defaultProofOfDeliveryRuleValidator(): ProofOfDeliveryRuleValidator {
    return ProofOfDeliveryRuleValidator(
        mutableListOf<ProofOfDeliveryRuleRule>(
            ProofOfDeliveryRuleCeilingRangeRule(2.2, 2363.6),
            ProofOfDeliveryRuleDurationMinutesRangeRule(80, 1000),
            ProofOfDeliveryRuleLabelLengthRule(1000),
        ),
    )
}
