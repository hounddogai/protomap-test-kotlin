package com.acme.transport.network.zone.reservation

import com.acme.transport.kit.text.normalize

/** A rule a regional zone ledger must pass. Check returns its problems, or none. */
interface RegionalZoneLedgerRule {
    fun check(item: RegionalZoneLedger): MutableList<String>
}

data class RegionalZoneLedgerRequiredCodeRule(var message: String) : RegionalZoneLedgerRule {
    override fun check(item: RegionalZoneLedger): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RegionalZoneLedgerAllowedTitleRule(var allowed: MutableList<String>) : RegionalZoneLedgerRule {
    override fun check(item: RegionalZoneLedger): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.title))) {
            return mutableListOf<String>("title ${item.title} is unknown")
        }
        return mutableListOf<String>()
    }
}

data class RegionalZoneLedgerCodePrefixRule(var prefix: String) : RegionalZoneLedgerRule {
    override fun check(item: RegionalZoneLedger): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class RegionalZoneLedgerRequiredTitleRule(var message: String) : RegionalZoneLedgerRule {
    override fun check(item: RegionalZoneLedger): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RegionalZoneLedgerTitleLengthRule(var maxLength: Int) : RegionalZoneLedgerRule {
    override fun check(item: RegionalZoneLedger): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RegionalZoneLedgerDriverIdLengthRule(var maxLength: Int) : RegionalZoneLedgerRule {
    override fun check(item: RegionalZoneLedger): MutableList<String> {
        val size = item.driverId.length
        if (size > maxLength) {
            return mutableListOf<String>("driver id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a regional zone ledger. */
data class RegionalZoneLedgerValidator(var rules: MutableList<RegionalZoneLedgerRule>) {
    fun validate(item: RegionalZoneLedger): MutableList<String> {
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

    fun requireValid(item: RegionalZoneLedger) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RegionalZoneLedger>): MutableMap<String, MutableList<String>> {
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

fun defaultRegionalZoneLedgerValidator(): RegionalZoneLedgerValidator {
    return RegionalZoneLedgerValidator(
        mutableListOf<RegionalZoneLedgerRule>(
            RegionalZoneLedgerRequiredCodeRule("regional zone ledger needs this field"),
            RegionalZoneLedgerAllowedTitleRule(mutableListOf<String>("fallback", "pending", "primary")),
            RegionalZoneLedgerCodePrefixRule("OPS"),
            RegionalZoneLedgerRequiredTitleRule("regional zone ledger needs a value"),
            RegionalZoneLedgerTitleLengthRule(86400),
            RegionalZoneLedgerDriverIdLengthRule(3600),
        ),
    )
}
