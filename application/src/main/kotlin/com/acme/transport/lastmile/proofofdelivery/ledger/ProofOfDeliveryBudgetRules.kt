package com.acme.transport.lastmile.proofofdelivery.ledger

/** A rule a proof of delivery budget must pass. Check returns its problems, or none. */
interface ProofOfDeliveryBudgetRule {
    fun check(item: ProofOfDeliveryBudget): MutableList<String>
}

data class ProofOfDeliveryBudgetRequiredNameRule(var message: String) : ProofOfDeliveryBudgetRule {
    override fun check(item: ProofOfDeliveryBudget): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ProofOfDeliveryBudgetRequiredCarrierRule(var message: String) : ProofOfDeliveryBudgetRule {
    override fun check(item: ProofOfDeliveryBudget): MutableList<String> {
        if (item.carrier.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ProofOfDeliveryBudgetCarrierLengthRule(var maxLength: Int) : ProofOfDeliveryBudgetRule {
    override fun check(item: ProofOfDeliveryBudget): MutableList<String> {
        val size = item.carrier.length
        if (size > maxLength) {
            return mutableListOf<String>("carrier is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ProofOfDeliveryBudgetNameLengthRule(var maxLength: Int) : ProofOfDeliveryBudgetRule {
    override fun check(item: ProofOfDeliveryBudget): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ProofOfDeliveryBudgetTrailerIdLengthRule(var maxLength: Int) : ProofOfDeliveryBudgetRule {
    override fun check(item: ProofOfDeliveryBudget): MutableList<String> {
        val size = item.trailerId.length
        if (size > maxLength) {
            return mutableListOf<String>("trailer id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ProofOfDeliveryBudgetCodePrefixRule(var prefix: String) : ProofOfDeliveryBudgetRule {
    override fun check(item: ProofOfDeliveryBudget): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a proof of delivery budget. */
data class ProofOfDeliveryBudgetValidator(var rules: MutableList<ProofOfDeliveryBudgetRule>) {
    fun validate(item: ProofOfDeliveryBudget): MutableList<String> {
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

    fun requireValid(item: ProofOfDeliveryBudget) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ProofOfDeliveryBudget>): MutableMap<String, MutableList<String>> {
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

fun defaultProofOfDeliveryBudgetValidator(): ProofOfDeliveryBudgetValidator {
    return ProofOfDeliveryBudgetValidator(
        mutableListOf<ProofOfDeliveryBudgetRule>(
            ProofOfDeliveryBudgetRequiredNameRule("proof of delivery budget needs this field"),
            ProofOfDeliveryBudgetRequiredCarrierRule("proof of delivery budget needs an entry"),
            ProofOfDeliveryBudgetCarrierLengthRule(16),
            ProofOfDeliveryBudgetNameLengthRule(120),
            ProofOfDeliveryBudgetTrailerIdLengthRule(3600),
            ProofOfDeliveryBudgetCodePrefixRule("OPS"),
        ),
    )
}
