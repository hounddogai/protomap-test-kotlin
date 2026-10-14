package com.acme.transport.crewing.qualification.schedule

import com.acme.transport.kit.text.normalize

/** A rule a tiered qualification ledger must pass. Check returns its problems, or none. */
interface TieredQualificationLedgerRule {
    fun check(item: TieredQualificationLedger): MutableList<String>
}

data class TieredQualificationLedgerRequiredCodeRule(var message: String) : TieredQualificationLedgerRule {
    override fun check(item: TieredQualificationLedger): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TieredQualificationLedgerTitleLengthRule(var maxLength: Int) : TieredQualificationLedgerRule {
    override fun check(item: TieredQualificationLedger): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TieredQualificationLedgerAllowedTitleRule(
    var allowed: MutableList<String>,
) : TieredQualificationLedgerRule {
    override fun check(item: TieredQualificationLedger): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.title))) {
            return mutableListOf<String>("title ${item.title} is not allowed")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a tiered qualification ledger. */
data class TieredQualificationLedgerValidator(var rules: MutableList<TieredQualificationLedgerRule>) {
    fun validate(item: TieredQualificationLedger): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: TieredQualificationLedger) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TieredQualificationLedger>): MutableMap<String, MutableList<String>> {
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

fun defaultTieredQualificationLedgerValidator(): TieredQualificationLedgerValidator {
    return TieredQualificationLedgerValidator(
        mutableListOf<TieredQualificationLedgerRule>(
            TieredQualificationLedgerRequiredCodeRule("tiered qualification ledger needs a value"),
            TieredQualificationLedgerTitleLengthRule(1000),
            TieredQualificationLedgerAllowedTitleRule(mutableListOf<String>()),
        ),
    )
}
