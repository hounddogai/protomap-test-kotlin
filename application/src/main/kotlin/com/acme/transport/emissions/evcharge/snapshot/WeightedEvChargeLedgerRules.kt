package com.acme.transport.emissions.evcharge.snapshot

import com.acme.transport.kit.text.normalize

/** A rule a weighted ev charge ledger must pass. Check returns its problems, or none. */
interface WeightedEvChargeLedgerRule {
    fun check(item: WeightedEvChargeLedger): MutableList<String>
}

data class WeightedEvChargeLedgerCodePrefixRule(var prefix: String) : WeightedEvChargeLedgerRule {
    override fun check(item: WeightedEvChargeLedger): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class WeightedEvChargeLedgerCodeLengthRule(var maxLength: Int) : WeightedEvChargeLedgerRule {
    override fun check(item: WeightedEvChargeLedger): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class WeightedEvChargeLedgerTitleLengthRule(var maxLength: Int) : WeightedEvChargeLedgerRule {
    override fun check(item: WeightedEvChargeLedger): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class WeightedEvChargeLedgerAllowedCodeRule(
    var allowed: MutableList<String>,
) : WeightedEvChargeLedgerRule {
    override fun check(item: WeightedEvChargeLedger): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is missing")
        }
        return mutableListOf<String>()
    }
}

data class WeightedEvChargeLedgerRequiredChecksumRule(var message: String) : WeightedEvChargeLedgerRule {
    override fun check(item: WeightedEvChargeLedger): MutableList<String> {
        if (item.checksum.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a weighted ev charge ledger. */
data class WeightedEvChargeLedgerValidator(var rules: MutableList<WeightedEvChargeLedgerRule>) {
    fun validate(item: WeightedEvChargeLedger): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: WeightedEvChargeLedger) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<WeightedEvChargeLedger>): MutableMap<String, MutableList<String>> {
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

fun defaultWeightedEvChargeLedgerValidator(): WeightedEvChargeLedgerValidator {
    return WeightedEvChargeLedgerValidator(
        mutableListOf<WeightedEvChargeLedgerRule>(
            WeightedEvChargeLedgerCodePrefixRule("OPS"),
            WeightedEvChargeLedgerCodeLengthRule(64),
            WeightedEvChargeLedgerTitleLengthRule(3600),
            WeightedEvChargeLedgerAllowedCodeRule(mutableListOf<String>("batched", "tiered")),
            WeightedEvChargeLedgerRequiredChecksumRule("weighted ev charge ledger needs this field"),
        ),
    )
}
