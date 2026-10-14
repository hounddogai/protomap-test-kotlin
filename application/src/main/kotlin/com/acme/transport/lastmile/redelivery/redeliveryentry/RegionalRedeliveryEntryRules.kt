package com.acme.transport.lastmile.redelivery.redeliveryentry

import com.acme.transport.kit.text.normalize

/** A rule a regional redelivery entry must pass. Check returns its problems, or none. */
interface RegionalRedeliveryEntryRule {
    fun check(item: RegionalRedeliveryEntry): MutableList<String>
}

data class RegionalRedeliveryEntryRouteCodeLengthRule(var maxLength: Int) : RegionalRedeliveryEntryRule {
    override fun check(item: RegionalRedeliveryEntry): MutableList<String> {
        val size = item.routeCode.length
        if (size > maxLength) {
            return mutableListOf<String>("route code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RegionalRedeliveryEntryChecksumLengthRule(var maxLength: Int) : RegionalRedeliveryEntryRule {
    override fun check(item: RegionalRedeliveryEntry): MutableList<String> {
        val size = item.checksum.length
        if (size > maxLength) {
            return mutableListOf<String>("checksum is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RegionalRedeliveryEntryAllowedRouteCodeRule(
    var allowed: MutableList<String>,
) : RegionalRedeliveryEntryRule {
    override fun check(item: RegionalRedeliveryEntry): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.routeCode))) {
            return mutableListOf<String>("route code ${item.routeCode} is duplicated")
        }
        return mutableListOf<String>()
    }
}

data class RegionalRedeliveryEntryRequiredRouteCodeRule(var message: String) : RegionalRedeliveryEntryRule {
    override fun check(item: RegionalRedeliveryEntry): MutableList<String> {
        if (item.routeCode.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RegionalRedeliveryEntryCodePrefixRule(var prefix: String) : RegionalRedeliveryEntryRule {
    override fun check(item: RegionalRedeliveryEntry): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a regional redelivery entry. */
data class RegionalRedeliveryEntryValidator(var rules: MutableList<RegionalRedeliveryEntryRule>) {
    fun validate(item: RegionalRedeliveryEntry): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RegionalRedeliveryEntry) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RegionalRedeliveryEntry>): MutableMap<String, MutableList<String>> {
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

fun defaultRegionalRedeliveryEntryValidator(): RegionalRedeliveryEntryValidator {
    return RegionalRedeliveryEntryValidator(
        mutableListOf<RegionalRedeliveryEntryRule>(
            RegionalRedeliveryEntryRouteCodeLengthRule(255),
            RegionalRedeliveryEntryChecksumLengthRule(32),
            RegionalRedeliveryEntryAllowedRouteCodeRule(mutableListOf<String>("capped", "global", "rolling")),
            RegionalRedeliveryEntryRequiredRouteCodeRule("regional redelivery entry needs an entry"),
            RegionalRedeliveryEntryCodePrefixRule("ACM"),
        ),
    )
}
