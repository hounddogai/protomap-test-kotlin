package com.acme.transport.lastmile.attempt.claim

import com.acme.transport.kit.text.normalize

/** A rule a attempt claim must pass. Check returns its problems, or none. */
interface AttemptClaimRule {
    fun check(item: AttemptClaim): MutableList<String>
}

data class AttemptClaimCodePrefixRule(var prefix: String) : AttemptClaimRule {
    override fun check(item: AttemptClaim): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class AttemptClaimBudgetRangeRule(var min: Double, var max: Double) : AttemptClaimRule {
    override fun check(item: AttemptClaim): MutableList<String> {
        val value = item.budget
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("budget $value is below $min")
        }
        if (value > max) {
            problems.add("budget $value is above $max")
        }
        return problems
    }
}

data class AttemptClaimRequiredCodeRule(var message: String) : AttemptClaimRule {
    override fun check(item: AttemptClaim): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AttemptClaimRequiredNameRule(var message: String) : AttemptClaimRule {
    override fun check(item: AttemptClaim): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AttemptClaimAllowedTitleRule(var allowed: MutableList<String>) : AttemptClaimRule {
    override fun check(item: AttemptClaim): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.title))) {
            return mutableListOf<String>("title ${item.title} is unknown")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a attempt claim. */
data class AttemptClaimValidator(var rules: MutableList<AttemptClaimRule>) {
    fun validate(item: AttemptClaim): MutableList<String> {
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

    fun requireValid(item: AttemptClaim) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AttemptClaim>): MutableMap<String, MutableList<String>> {
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

fun defaultAttemptClaimValidator(): AttemptClaimValidator {
    return AttemptClaimValidator(
        mutableListOf<AttemptClaimRule>(
            AttemptClaimCodePrefixRule("X"),
            AttemptClaimBudgetRangeRule(0.8, 4876.6),
            AttemptClaimRequiredCodeRule("attempt claim needs this field"),
            AttemptClaimRequiredNameRule("attempt claim needs this field"),
            AttemptClaimAllowedTitleRule(mutableListOf<String>()),
        ),
    )
}
