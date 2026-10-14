package com.acme.transport.lastmile.drop.rollup

import com.acme.transport.kit.text.normalize

/** A rule a regional drop tier must pass. Check returns its problems, or none. */
interface RegionalDropTierRule {
    fun check(item: RegionalDropTier): MutableList<String>
}

data class RegionalDropTierLocaleLengthRule(var maxLength: Int) : RegionalDropTierRule {
    override fun check(item: RegionalDropTier): MutableList<String> {
        val size = item.locale.length
        if (size > maxLength) {
            return mutableListOf<String>("locale is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RegionalDropTierAllowedContactEmailRule(var allowed: MutableList<String>) : RegionalDropTierRule {
    override fun check(item: RegionalDropTier): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.contactEmail))) {
            return mutableListOf<String>("contact email ${item.contactEmail} is below minimum")
        }
        return mutableListOf<String>()
    }
}

data class RegionalDropTierRequiredTitleRule(var message: String) : RegionalDropTierRule {
    override fun check(item: RegionalDropTier): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RegionalDropTierRequiredContactEmailRule(var message: String) : RegionalDropTierRule {
    override fun check(item: RegionalDropTier): MutableList<String> {
        if (item.contactEmail.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RegionalDropTierRequiredLocaleRule(var message: String) : RegionalDropTierRule {
    override fun check(item: RegionalDropTier): MutableList<String> {
        if (item.locale.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RegionalDropTierCodePrefixRule(var prefix: String) : RegionalDropTierRule {
    override fun check(item: RegionalDropTier): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a regional drop tier. */
data class RegionalDropTierValidator(var rules: MutableList<RegionalDropTierRule>) {
    fun validate(item: RegionalDropTier): MutableList<String> {
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

    fun requireValid(item: RegionalDropTier) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RegionalDropTier>): MutableMap<String, MutableList<String>> {
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

fun defaultRegionalDropTierValidator(): RegionalDropTierValidator {
    return RegionalDropTierValidator(
        mutableListOf<RegionalDropTierRule>(
            RegionalDropTierLocaleLengthRule(1000),
            RegionalDropTierAllowedContactEmailRule(mutableListOf<String>("fallback")),
            RegionalDropTierRequiredTitleRule("regional drop tier needs this field"),
            RegionalDropTierRequiredContactEmailRule("regional drop tier needs this field"),
            RegionalDropTierRequiredLocaleRule("regional drop tier needs a value"),
            RegionalDropTierCodePrefixRule("REF"),
        ),
    )
}
