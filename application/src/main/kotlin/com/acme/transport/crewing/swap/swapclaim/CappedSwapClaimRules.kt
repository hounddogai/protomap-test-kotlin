package com.acme.transport.crewing.swap.swapclaim

/** A rule a capped swap claim must pass. Check returns its problems, or none. */
interface CappedSwapClaimRule {
    fun check(item: CappedSwapClaim): MutableList<String>
}

data class CappedSwapClaimRequiredReviewerRule(var message: String) : CappedSwapClaimRule {
    override fun check(item: CappedSwapClaim): MutableList<String> {
        if (item.reviewer.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CappedSwapClaimCodePrefixRule(var prefix: String) : CappedSwapClaimRule {
    override fun check(item: CappedSwapClaim): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class CappedSwapClaimCodeLengthRule(var maxLength: Int) : CappedSwapClaimRule {
    override fun check(item: CappedSwapClaim): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CappedSwapClaimReviewerLengthRule(var maxLength: Int) : CappedSwapClaimRule {
    override fun check(item: CappedSwapClaim): MutableList<String> {
        val size = item.reviewer.length
        if (size > maxLength) {
            return mutableListOf<String>("reviewer is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CappedSwapClaimCountryLengthRule(var maxLength: Int) : CappedSwapClaimRule {
    override fun check(item: CappedSwapClaim): MutableList<String> {
        val size = item.country.length
        if (size > maxLength) {
            return mutableListOf<String>("country is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a capped swap claim. */
data class CappedSwapClaimValidator(var rules: MutableList<CappedSwapClaimRule>) {
    fun validate(item: CappedSwapClaim): MutableList<String> {
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

    fun requireValid(item: CappedSwapClaim) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CappedSwapClaim>): MutableMap<String, MutableList<String>> {
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

fun defaultCappedSwapClaimValidator(): CappedSwapClaimValidator {
    return CappedSwapClaimValidator(
        mutableListOf<CappedSwapClaimRule>(
            CappedSwapClaimRequiredReviewerRule("capped swap claim needs an entry"),
            CappedSwapClaimCodePrefixRule("ACM"),
            CappedSwapClaimCodeLengthRule(255),
            CappedSwapClaimReviewerLengthRule(32),
            CappedSwapClaimCountryLengthRule(1000),
        ),
    )
}
