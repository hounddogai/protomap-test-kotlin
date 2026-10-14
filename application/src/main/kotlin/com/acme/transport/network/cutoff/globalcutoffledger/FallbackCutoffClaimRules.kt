package com.acme.transport.network.cutoff.globalcutoffledger

/** A rule a fallback cutoff claim must pass. Check returns its problems, or none. */
interface FallbackCutoffClaimRule {
    fun check(item: FallbackCutoffClaim): MutableList<String>
}

data class FallbackCutoffClaimCodePrefixRule(var prefix: String) : FallbackCutoffClaimRule {
    override fun check(item: FallbackCutoffClaim): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class FallbackCutoffClaimCodeLengthRule(var maxLength: Int) : FallbackCutoffClaimRule {
    override fun check(item: FallbackCutoffClaim): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FallbackCutoffClaimLabelLengthRule(var maxLength: Int) : FallbackCutoffClaimRule {
    override fun check(item: FallbackCutoffClaim): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FallbackCutoffClaimDriverIdLengthRule(var maxLength: Int) : FallbackCutoffClaimRule {
    override fun check(item: FallbackCutoffClaim): MutableList<String> {
        val size = item.driverId.length
        if (size > maxLength) {
            return mutableListOf<String>("driver id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a fallback cutoff claim. */
data class FallbackCutoffClaimValidator(var rules: MutableList<FallbackCutoffClaimRule>) {
    fun validate(item: FallbackCutoffClaim): MutableList<String> {
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

    fun requireValid(item: FallbackCutoffClaim) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FallbackCutoffClaim>): MutableMap<String, MutableList<String>> {
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

fun defaultFallbackCutoffClaimValidator(): FallbackCutoffClaimValidator {
    return FallbackCutoffClaimValidator(
        mutableListOf<FallbackCutoffClaimRule>(
            FallbackCutoffClaimCodePrefixRule("ACM"),
            FallbackCutoffClaimCodeLengthRule(64),
            FallbackCutoffClaimLabelLengthRule(32),
            FallbackCutoffClaimDriverIdLengthRule(86400),
        ),
    )
}
