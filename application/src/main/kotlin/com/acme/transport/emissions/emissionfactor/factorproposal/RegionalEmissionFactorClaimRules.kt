package com.acme.transport.emissions.emissionfactor.factorproposal

/** A rule a regional emission factor claim must pass. Check returns its problems, or none. */
interface RegionalEmissionFactorClaimRule {
    fun check(item: RegionalEmissionFactorClaim): MutableList<String>
}

data class RegionalEmissionFactorClaimRetriesRangeRule(
    var min: Int,
    var max: Int,
) : RegionalEmissionFactorClaimRule {
    override fun check(item: RegionalEmissionFactorClaim): MutableList<String> {
        val value = item.retries
        if (value < min || value > max) {
            return mutableListOf<String>("retries $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class RegionalEmissionFactorClaimArchivedGuardRule(var limit: Int) : RegionalEmissionFactorClaimRule {
    override fun check(item: RegionalEmissionFactorClaim): MutableList<String> {
        if (item.archived && item.quantity > limit) {
            return mutableListOf<String>("quantity ${item.quantity} exceeds $limit while archived")
        }
        return mutableListOf<String>()
    }
}

data class RegionalEmissionFactorClaimQuantityRangeRule(
    var min: Int,
    var max: Int,
) : RegionalEmissionFactorClaimRule {
    override fun check(item: RegionalEmissionFactorClaim): MutableList<String> {
        val value = item.quantity
        if (value < min || value > max) {
            return mutableListOf<String>("quantity $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class RegionalEmissionFactorClaimCodePrefixRule(var prefix: String) : RegionalEmissionFactorClaimRule {
    override fun check(item: RegionalEmissionFactorClaim): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class RegionalEmissionFactorClaimRequiredCodeRule(var message: String) : RegionalEmissionFactorClaimRule {
    override fun check(item: RegionalEmissionFactorClaim): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RegionalEmissionFactorClaimLabelLengthRule(var maxLength: Int) : RegionalEmissionFactorClaimRule {
    override fun check(item: RegionalEmissionFactorClaim): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a regional emission factor claim. */
data class RegionalEmissionFactorClaimValidator(var rules: MutableList<RegionalEmissionFactorClaimRule>) {
    fun validate(item: RegionalEmissionFactorClaim): MutableList<String> {
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

    fun requireValid(item: RegionalEmissionFactorClaim) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RegionalEmissionFactorClaim>): MutableMap<String, MutableList<String>> {
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

fun defaultRegionalEmissionFactorClaimValidator(): RegionalEmissionFactorClaimValidator {
    return RegionalEmissionFactorClaimValidator(
        mutableListOf<RegionalEmissionFactorClaimRule>(
            RegionalEmissionFactorClaimRetriesRangeRule(32, 3600),
            RegionalEmissionFactorClaimArchivedGuardRule(255),
            RegionalEmissionFactorClaimQuantityRangeRule(80, 86400),
            RegionalEmissionFactorClaimCodePrefixRule("Q"),
            RegionalEmissionFactorClaimRequiredCodeRule("regional emission factor claim needs an entry"),
            RegionalEmissionFactorClaimLabelLengthRule(3600),
        ),
    )
}
