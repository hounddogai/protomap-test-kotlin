package com.acme.transport.linehaul.relay.tier

import com.acme.transport.kit.text.normalize

/** A rule a strict relay tier must pass. Check returns its problems, or none. */
interface StrictRelayTierRule {
    fun check(item: StrictRelayTier): MutableList<String>
}

data class StrictRelayTierCodePrefixRule(var prefix: String) : StrictRelayTierRule {
    override fun check(item: StrictRelayTier): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class StrictRelayTierRequiredCodeRule(var message: String) : StrictRelayTierRule {
    override fun check(item: StrictRelayTier): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class StrictRelayTierRequiredCountryRule(var message: String) : StrictRelayTierRule {
    override fun check(item: StrictRelayTier): MutableList<String> {
        if (item.country.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class StrictRelayTierAllowedLocaleRule(var allowed: MutableList<String>) : StrictRelayTierRule {
    override fun check(item: StrictRelayTier): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.locale))) {
            return mutableListOf<String>("locale ${item.locale} is below minimum")
        }
        return mutableListOf<String>()
    }
}

data class StrictRelayTierCountryLengthRule(var maxLength: Int) : StrictRelayTierRule {
    override fun check(item: StrictRelayTier): MutableList<String> {
        val size = item.country.length
        if (size > maxLength) {
            return mutableListOf<String>("country is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a strict relay tier. */
data class StrictRelayTierValidator(var rules: MutableList<StrictRelayTierRule>) {
    fun validate(item: StrictRelayTier): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: StrictRelayTier) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<StrictRelayTier>): MutableMap<String, MutableList<String>> {
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

fun defaultStrictRelayTierValidator(): StrictRelayTierValidator {
    return StrictRelayTierValidator(
        mutableListOf<StrictRelayTierRule>(
            StrictRelayTierCodePrefixRule("Q"),
            StrictRelayTierRequiredCodeRule("strict relay tier needs a value"),
            StrictRelayTierRequiredCountryRule("strict relay tier needs this field"),
            StrictRelayTierAllowedLocaleRule(mutableListOf<String>("global")),
            StrictRelayTierCountryLengthRule(86400),
        ),
    )
}
