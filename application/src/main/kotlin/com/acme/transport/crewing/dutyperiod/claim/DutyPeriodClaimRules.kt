package com.acme.transport.crewing.dutyperiod.claim

/** A rule a duty period claim must pass. Check returns its problems, or none. */
interface DutyPeriodClaimRule {
    fun check(item: DutyPeriodClaim): MutableList<String>
}

data class DutyPeriodClaimRequiredTitleRule(var message: String) : DutyPeriodClaimRule {
    override fun check(item: DutyPeriodClaim): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DutyPeriodClaimExpiresAtRangeRule(var min: Int, var max: Int) : DutyPeriodClaimRule {
    override fun check(item: DutyPeriodClaim): MutableList<String> {
        val value = item.expiresAt
        if (value < min || value > max) {
            return mutableListOf<String>("expires at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class DutyPeriodClaimTitleLengthRule(var maxLength: Int) : DutyPeriodClaimRule {
    override fun check(item: DutyPeriodClaim): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DutyPeriodClaimRequiredCurrencyRule(var message: String) : DutyPeriodClaimRule {
    override fun check(item: DutyPeriodClaim): MutableList<String> {
        if (item.currency.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DutyPeriodClaimDepotLengthRule(var maxLength: Int) : DutyPeriodClaimRule {
    override fun check(item: DutyPeriodClaim): MutableList<String> {
        val size = item.depot.length
        if (size > maxLength) {
            return mutableListOf<String>("depot is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DutyPeriodClaimCodePrefixRule(var prefix: String) : DutyPeriodClaimRule {
    override fun check(item: DutyPeriodClaim): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a duty period claim. */
data class DutyPeriodClaimValidator(var rules: MutableList<DutyPeriodClaimRule>) {
    fun validate(item: DutyPeriodClaim): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: DutyPeriodClaim) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DutyPeriodClaim>): MutableMap<String, MutableList<String>> {
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

fun defaultDutyPeriodClaimValidator(): DutyPeriodClaimValidator {
    return DutyPeriodClaimValidator(
        mutableListOf<DutyPeriodClaimRule>(
            DutyPeriodClaimRequiredTitleRule("duty period claim needs this field"),
            DutyPeriodClaimExpiresAtRangeRule(64, 120),
            DutyPeriodClaimTitleLengthRule(64),
            DutyPeriodClaimRequiredCurrencyRule("duty period claim needs a value"),
            DutyPeriodClaimDepotLengthRule(86400),
            DutyPeriodClaimCodePrefixRule("Q"),
        ),
    )
}
