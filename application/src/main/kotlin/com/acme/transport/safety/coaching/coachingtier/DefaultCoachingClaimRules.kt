package com.acme.transport.safety.coaching.coachingtier

/** A rule a default coaching claim must pass. Check returns its problems, or none. */
interface DefaultCoachingClaimRule {
    fun check(item: DefaultCoachingClaim): MutableList<String>
}

data class DefaultCoachingClaimSequenceRangeRule(var min: Int, var max: Int) : DefaultCoachingClaimRule {
    override fun check(item: DefaultCoachingClaim): MutableList<String> {
        val value = item.sequence
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("sequence $value is below $min")
        }
        if (value > max) {
            problems.add("sequence $value is above $max")
        }
        return problems
    }
}

data class DefaultCoachingClaimSummaryLengthRule(var maxLength: Int) : DefaultCoachingClaimRule {
    override fun check(item: DefaultCoachingClaim): MutableList<String> {
        val size = item.summary.length
        if (size > maxLength) {
            return mutableListOf<String>("summary is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DefaultCoachingClaimRequiredSummaryRule(var message: String) : DefaultCoachingClaimRule {
    override fun check(item: DefaultCoachingClaim): MutableList<String> {
        if (item.summary.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DefaultCoachingClaimCodePrefixRule(var prefix: String) : DefaultCoachingClaimRule {
    override fun check(item: DefaultCoachingClaim): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class DefaultCoachingClaimCodeLengthRule(var maxLength: Int) : DefaultCoachingClaimRule {
    override fun check(item: DefaultCoachingClaim): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DefaultCoachingClaimRequiredCodeRule(var message: String) : DefaultCoachingClaimRule {
    override fun check(item: DefaultCoachingClaim): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a default coaching claim. */
data class DefaultCoachingClaimValidator(var rules: MutableList<DefaultCoachingClaimRule>) {
    fun validate(item: DefaultCoachingClaim): MutableList<String> {
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

    fun requireValid(item: DefaultCoachingClaim) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DefaultCoachingClaim>): MutableMap<String, MutableList<String>> {
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

fun defaultDefaultCoachingClaimValidator(): DefaultCoachingClaimValidator {
    return DefaultCoachingClaimValidator(
        mutableListOf<DefaultCoachingClaimRule>(
            DefaultCoachingClaimSequenceRangeRule(64, 86400),
            DefaultCoachingClaimSummaryLengthRule(86400),
            DefaultCoachingClaimRequiredSummaryRule("default coaching claim needs an entry"),
            DefaultCoachingClaimCodePrefixRule("X"),
            DefaultCoachingClaimCodeLengthRule(255),
            DefaultCoachingClaimRequiredCodeRule("default coaching claim needs an entry"),
        ),
    )
}
