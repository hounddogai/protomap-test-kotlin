package com.acme.transport.network.zone.bundle

import com.acme.transport.kit.text.normalize

/** A rule a weighted zone ledger must pass. Check returns its problems, or none. */
interface WeightedZoneLedgerRule {
    fun check(item: WeightedZoneLedger): MutableList<String>
}

data class WeightedZoneLedgerCodePrefixRule(var prefix: String) : WeightedZoneLedgerRule {
    override fun check(item: WeightedZoneLedger): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class WeightedZoneLedgerAllowedTitleRule(var allowed: MutableList<String>) : WeightedZoneLedgerRule {
    override fun check(item: WeightedZoneLedger): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.title))) {
            return mutableListOf<String>("title ${item.title} is stale")
        }
        return mutableListOf<String>()
    }
}

data class WeightedZoneLedgerCodeLengthRule(var maxLength: Int) : WeightedZoneLedgerRule {
    override fun check(item: WeightedZoneLedger): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class WeightedZoneLedgerContactEmailLengthRule(var maxLength: Int) : WeightedZoneLedgerRule {
    override fun check(item: WeightedZoneLedger): MutableList<String> {
        val size = item.contactEmail.length
        if (size > maxLength) {
            return mutableListOf<String>("contact email is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class WeightedZoneLedgerLocaleLengthRule(var maxLength: Int) : WeightedZoneLedgerRule {
    override fun check(item: WeightedZoneLedger): MutableList<String> {
        val size = item.locale.length
        if (size > maxLength) {
            return mutableListOf<String>("locale is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a weighted zone ledger. */
data class WeightedZoneLedgerValidator(var rules: MutableList<WeightedZoneLedgerRule>) {
    fun validate(item: WeightedZoneLedger): MutableList<String> {
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

    fun requireValid(item: WeightedZoneLedger) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<WeightedZoneLedger>): MutableMap<String, MutableList<String>> {
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

fun defaultWeightedZoneLedgerValidator(): WeightedZoneLedgerValidator {
    return WeightedZoneLedgerValidator(
        mutableListOf<WeightedZoneLedgerRule>(
            WeightedZoneLedgerCodePrefixRule("X"),
            WeightedZoneLedgerAllowedTitleRule(mutableListOf<String>("regional", "batched")),
            WeightedZoneLedgerCodeLengthRule(3600),
            WeightedZoneLedgerContactEmailLengthRule(255),
            WeightedZoneLedgerLocaleLengthRule(64),
        ),
    )
}
