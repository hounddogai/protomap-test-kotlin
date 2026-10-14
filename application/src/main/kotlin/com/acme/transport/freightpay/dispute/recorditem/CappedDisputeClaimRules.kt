package com.acme.transport.freightpay.dispute.recorditem

/** A rule a capped dispute claim must pass. Check returns its problems, or none. */
interface CappedDisputeClaimRule {
    fun check(item: CappedDisputeClaim): MutableList<String>
}

data class CappedDisputeClaimRequiredCodeRule(var message: String) : CappedDisputeClaimRule {
    override fun check(item: CappedDisputeClaim): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CappedDisputeClaimPalletsRangeRule(var min: Int, var max: Int) : CappedDisputeClaimRule {
    override fun check(item: CappedDisputeClaim): MutableList<String> {
        val value = item.pallets
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("pallets $value is below $min")
        }
        if (value > max) {
            problems.add("pallets $value is above $max")
        }
        return problems
    }
}

data class CappedDisputeClaimRequiredRouteCodeRule(var message: String) : CappedDisputeClaimRule {
    override fun check(item: CappedDisputeClaim): MutableList<String> {
        if (item.routeCode.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CappedDisputeClaimCodePrefixRule(var prefix: String) : CappedDisputeClaimRule {
    override fun check(item: CappedDisputeClaim): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class CappedDisputeClaimRequiredCarrierRule(var message: String) : CappedDisputeClaimRule {
    override fun check(item: CappedDisputeClaim): MutableList<String> {
        if (item.carrier.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a capped dispute claim. */
data class CappedDisputeClaimValidator(var rules: MutableList<CappedDisputeClaimRule>) {
    fun validate(item: CappedDisputeClaim): MutableList<String> {
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

    fun requireValid(item: CappedDisputeClaim) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CappedDisputeClaim>): MutableMap<String, MutableList<String>> {
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

fun defaultCappedDisputeClaimValidator(): CappedDisputeClaimValidator {
    return CappedDisputeClaimValidator(
        mutableListOf<CappedDisputeClaimRule>(
            CappedDisputeClaimRequiredCodeRule("capped dispute claim needs an entry"),
            CappedDisputeClaimPalletsRangeRule(1000, 86400),
            CappedDisputeClaimRequiredRouteCodeRule("capped dispute claim needs an entry"),
            CappedDisputeClaimCodePrefixRule("X"),
            CappedDisputeClaimRequiredCarrierRule("capped dispute claim needs this field"),
        ),
    )
}
