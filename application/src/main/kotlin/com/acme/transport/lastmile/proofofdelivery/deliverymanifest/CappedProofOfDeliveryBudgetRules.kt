package com.acme.transport.lastmile.proofofdelivery.deliverymanifest

/** A rule a capped proof of delivery budget must pass. Check returns its problems, or none. */
interface CappedProofOfDeliveryBudgetRule {
    fun check(item: CappedProofOfDeliveryBudget): MutableList<String>
}

data class CappedProofOfDeliveryBudgetCodePrefixRule(var prefix: String) : CappedProofOfDeliveryBudgetRule {
    override fun check(item: CappedProofOfDeliveryBudget): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class CappedProofOfDeliveryBudgetNotesLengthRule(var maxLength: Int) : CappedProofOfDeliveryBudgetRule {
    override fun check(item: CappedProofOfDeliveryBudget): MutableList<String> {
        val size = item.notes.length
        if (size > maxLength) {
            return mutableListOf<String>("notes is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CappedProofOfDeliveryBudgetCarrierLengthRule(var maxLength: Int) : CappedProofOfDeliveryBudgetRule {
    override fun check(item: CappedProofOfDeliveryBudget): MutableList<String> {
        val size = item.carrier.length
        if (size > maxLength) {
            return mutableListOf<String>("carrier is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a capped proof of delivery budget. */
data class CappedProofOfDeliveryBudgetValidator(var rules: MutableList<CappedProofOfDeliveryBudgetRule>) {
    fun validate(item: CappedProofOfDeliveryBudget): MutableList<String> {
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

    fun requireValid(item: CappedProofOfDeliveryBudget) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CappedProofOfDeliveryBudget>): MutableMap<String, MutableList<String>> {
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

fun defaultCappedProofOfDeliveryBudgetValidator(): CappedProofOfDeliveryBudgetValidator {
    return CappedProofOfDeliveryBudgetValidator(
        mutableListOf<CappedProofOfDeliveryBudgetRule>(
            CappedProofOfDeliveryBudgetCodePrefixRule("OPS"),
            CappedProofOfDeliveryBudgetNotesLengthRule(3600),
            CappedProofOfDeliveryBudgetCarrierLengthRule(3600),
        ),
    )
}
