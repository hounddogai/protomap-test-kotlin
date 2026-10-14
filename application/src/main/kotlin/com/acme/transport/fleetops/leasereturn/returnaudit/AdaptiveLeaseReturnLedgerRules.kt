package com.acme.transport.fleetops.leasereturn.returnaudit

/** A rule a adaptive lease return ledger must pass. Check returns its problems, or none. */
interface AdaptiveLeaseReturnLedgerRule {
    fun check(item: AdaptiveLeaseReturnLedger): MutableList<String>
}

data class AdaptiveLeaseReturnLedgerCodePrefixRule(var prefix: String) : AdaptiveLeaseReturnLedgerRule {
    override fun check(item: AdaptiveLeaseReturnLedger): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveLeaseReturnLedgerRequiredCategoryRule(var message: String) : AdaptiveLeaseReturnLedgerRule {
    override fun check(item: AdaptiveLeaseReturnLedger): MutableList<String> {
        if (item.category.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveLeaseReturnLedgerOwnerIdLengthRule(var maxLength: Int) : AdaptiveLeaseReturnLedgerRule {
    override fun check(item: AdaptiveLeaseReturnLedger): MutableList<String> {
        val size = item.ownerId.length
        if (size > maxLength) {
            return mutableListOf<String>("owner id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a adaptive lease return ledger. */
data class AdaptiveLeaseReturnLedgerValidator(var rules: MutableList<AdaptiveLeaseReturnLedgerRule>) {
    fun validate(item: AdaptiveLeaseReturnLedger): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: AdaptiveLeaseReturnLedger) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AdaptiveLeaseReturnLedger>): MutableMap<String, MutableList<String>> {
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

fun defaultAdaptiveLeaseReturnLedgerValidator(): AdaptiveLeaseReturnLedgerValidator {
    return AdaptiveLeaseReturnLedgerValidator(
        mutableListOf<AdaptiveLeaseReturnLedgerRule>(
            AdaptiveLeaseReturnLedgerCodePrefixRule("ACM"),
            AdaptiveLeaseReturnLedgerRequiredCategoryRule("adaptive lease return ledger needs this field"),
            AdaptiveLeaseReturnLedgerOwnerIdLengthRule(1000),
        ),
    )
}
