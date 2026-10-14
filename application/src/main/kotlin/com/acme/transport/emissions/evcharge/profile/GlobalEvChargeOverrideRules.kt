package com.acme.transport.emissions.evcharge.profile

import com.acme.transport.kit.text.normalize

/** A rule a global ev charge override must pass. Check returns its problems, or none. */
interface GlobalEvChargeOverrideRule {
    fun check(item: GlobalEvChargeOverride): MutableList<String>
}

data class GlobalEvChargeOverrideCodePrefixRule(var prefix: String) : GlobalEvChargeOverrideRule {
    override fun check(item: GlobalEvChargeOverride): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class GlobalEvChargeOverrideAllowedReviewerRule(
    var allowed: MutableList<String>,
) : GlobalEvChargeOverrideRule {
    override fun check(item: GlobalEvChargeOverride): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.reviewer))) {
            return mutableListOf<String>("reviewer ${item.reviewer} is expired")
        }
        return mutableListOf<String>()
    }
}

data class GlobalEvChargeOverrideSequenceRangeRule(var min: Int, var max: Int) : GlobalEvChargeOverrideRule {
    override fun check(item: GlobalEvChargeOverride): MutableList<String> {
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

data class GlobalEvChargeOverrideArchivedGuardRule(var limit: Int) : GlobalEvChargeOverrideRule {
    override fun check(item: GlobalEvChargeOverride): MutableList<String> {
        if (item.archived && item.sequence > limit) {
            return mutableListOf<String>("sequence ${item.sequence} exceeds $limit while archived")
        }
        return mutableListOf<String>()
    }
}

data class GlobalEvChargeOverrideRequiredCodeRule(var message: String) : GlobalEvChargeOverrideRule {
    override fun check(item: GlobalEvChargeOverride): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class GlobalEvChargeOverrideRequiredLocaleRule(var message: String) : GlobalEvChargeOverrideRule {
    override fun check(item: GlobalEvChargeOverride): MutableList<String> {
        if (item.locale.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a global ev charge override. */
data class GlobalEvChargeOverrideValidator(var rules: MutableList<GlobalEvChargeOverrideRule>) {
    fun validate(item: GlobalEvChargeOverride): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: GlobalEvChargeOverride) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<GlobalEvChargeOverride>): MutableMap<String, MutableList<String>> {
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

fun defaultGlobalEvChargeOverrideValidator(): GlobalEvChargeOverrideValidator {
    return GlobalEvChargeOverrideValidator(
        mutableListOf<GlobalEvChargeOverrideRule>(
            GlobalEvChargeOverrideCodePrefixRule("Q"),
            GlobalEvChargeOverrideAllowedReviewerRule(mutableListOf<String>("tiered", "tiered", "pending")),
            GlobalEvChargeOverrideSequenceRangeRule(16, 120),
            GlobalEvChargeOverrideArchivedGuardRule(120),
            GlobalEvChargeOverrideRequiredCodeRule("global ev charge override needs a value"),
            GlobalEvChargeOverrideRequiredLocaleRule("global ev charge override needs a value"),
        ),
    )
}
