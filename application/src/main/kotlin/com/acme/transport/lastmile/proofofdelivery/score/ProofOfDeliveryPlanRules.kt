package com.acme.transport.lastmile.proofofdelivery.score

/** A rule a proof of delivery plan must pass. Check returns its problems, or none. */
interface ProofOfDeliveryPlanRule {
    fun check(item: ProofOfDeliveryPlan): MutableList<String>
}

data class ProofOfDeliveryPlanDockLengthRule(var maxLength: Int) : ProofOfDeliveryPlanRule {
    override fun check(item: ProofOfDeliveryPlan): MutableList<String> {
        val size = item.dock.length
        if (size > maxLength) {
            return mutableListOf<String>("dock is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ProofOfDeliveryPlanCodePrefixRule(var prefix: String) : ProofOfDeliveryPlanRule {
    override fun check(item: ProofOfDeliveryPlan): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class ProofOfDeliveryPlanRequiredCurrencyRule(var message: String) : ProofOfDeliveryPlanRule {
    override fun check(item: ProofOfDeliveryPlan): MutableList<String> {
        if (item.currency.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ProofOfDeliveryPlanBatchSizeRangeRule(var min: Int, var max: Int) : ProofOfDeliveryPlanRule {
    override fun check(item: ProofOfDeliveryPlan): MutableList<String> {
        val value = item.batchSize
        if (value < min || value > max) {
            return mutableListOf<String>("batch size $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a proof of delivery plan. */
data class ProofOfDeliveryPlanValidator(var rules: MutableList<ProofOfDeliveryPlanRule>) {
    fun validate(item: ProofOfDeliveryPlan): MutableList<String> {
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

    fun requireValid(item: ProofOfDeliveryPlan) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ProofOfDeliveryPlan>): MutableMap<String, MutableList<String>> {
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

fun defaultProofOfDeliveryPlanValidator(): ProofOfDeliveryPlanValidator {
    return ProofOfDeliveryPlanValidator(
        mutableListOf<ProofOfDeliveryPlanRule>(
            ProofOfDeliveryPlanDockLengthRule(32),
            ProofOfDeliveryPlanCodePrefixRule("X"),
            ProofOfDeliveryPlanRequiredCurrencyRule("proof of delivery plan needs an entry"),
            ProofOfDeliveryPlanBatchSizeRangeRule(120, 3600),
        ),
    )
}
