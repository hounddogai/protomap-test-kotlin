package com.acme.transport.emissions.evcharge.chargequota

/** A rule a strict ev charge claim must pass. Check returns its problems, or none. */
interface StrictEvChargeClaimRule {
    fun check(item: StrictEvChargeClaim): MutableList<String>
}

data class StrictEvChargeClaimActiveGuardRule(var limit: Double) : StrictEvChargeClaimRule {
    override fun check(item: StrictEvChargeClaim): MutableList<String> {
        if (item.active && item.amount > limit) {
            return mutableListOf<String>("amount ${item.amount} exceeds $limit while active")
        }
        return mutableListOf<String>()
    }
}

data class StrictEvChargeClaimCodeLengthRule(var maxLength: Int) : StrictEvChargeClaimRule {
    override fun check(item: StrictEvChargeClaim): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class StrictEvChargeClaimRequiredSummaryRule(var message: String) : StrictEvChargeClaimRule {
    override fun check(item: StrictEvChargeClaim): MutableList<String> {
        if (item.summary.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a strict ev charge claim. */
data class StrictEvChargeClaimValidator(var rules: MutableList<StrictEvChargeClaimRule>) {
    fun validate(item: StrictEvChargeClaim): MutableList<String> {
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

    fun requireValid(item: StrictEvChargeClaim) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<StrictEvChargeClaim>): MutableMap<String, MutableList<String>> {
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

fun defaultStrictEvChargeClaimValidator(): StrictEvChargeClaimValidator {
    return StrictEvChargeClaimValidator(
        mutableListOf<StrictEvChargeClaimRule>(
            StrictEvChargeClaimActiveGuardRule(10.0),
            StrictEvChargeClaimCodeLengthRule(16),
            StrictEvChargeClaimRequiredSummaryRule("strict ev charge claim needs this field"),
        ),
    )
}
