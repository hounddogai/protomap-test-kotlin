package com.acme.transport.lastmile.redelivery.ledger

import com.acme.transport.kit.text.normalize

/** A rule a global redelivery ledger must pass. Check returns its problems, or none. */
interface GlobalRedeliveryLedgerRule {
    fun check(item: GlobalRedeliveryLedger): MutableList<String>
}

data class GlobalRedeliveryLedgerRequiredNameRule(var message: String) : GlobalRedeliveryLedgerRule {
    override fun check(item: GlobalRedeliveryLedger): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class GlobalRedeliveryLedgerNameLengthRule(var maxLength: Int) : GlobalRedeliveryLedgerRule {
    override fun check(item: GlobalRedeliveryLedger): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class GlobalRedeliveryLedgerAllowedNameRule(
    var allowed: MutableList<String>,
) : GlobalRedeliveryLedgerRule {
    override fun check(item: GlobalRedeliveryLedger): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.name))) {
            return mutableListOf<String>("name ${item.name} is too long")
        }
        return mutableListOf<String>()
    }
}

data class GlobalRedeliveryLedgerScoreRangeRule(var min: Double, var max: Double) : GlobalRedeliveryLedgerRule {
    override fun check(item: GlobalRedeliveryLedger): MutableList<String> {
        val value = item.score
        if (value < min || value > max) {
            return mutableListOf<String>("score $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class GlobalRedeliveryLedgerRequiredDepotRule(var message: String) : GlobalRedeliveryLedgerRule {
    override fun check(item: GlobalRedeliveryLedger): MutableList<String> {
        if (item.depot.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class GlobalRedeliveryLedgerCodeLengthRule(var maxLength: Int) : GlobalRedeliveryLedgerRule {
    override fun check(item: GlobalRedeliveryLedger): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a global redelivery ledger. */
data class GlobalRedeliveryLedgerValidator(var rules: MutableList<GlobalRedeliveryLedgerRule>) {
    fun validate(item: GlobalRedeliveryLedger): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: GlobalRedeliveryLedger) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<GlobalRedeliveryLedger>): MutableMap<String, MutableList<String>> {
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

fun defaultGlobalRedeliveryLedgerValidator(): GlobalRedeliveryLedgerValidator {
    return GlobalRedeliveryLedgerValidator(
        mutableListOf<GlobalRedeliveryLedgerRule>(
            GlobalRedeliveryLedgerRequiredNameRule("global redelivery ledger needs this field"),
            GlobalRedeliveryLedgerNameLengthRule(120),
            GlobalRedeliveryLedgerAllowedNameRule(mutableListOf<String>("strict", "primary", "legacy")),
            GlobalRedeliveryLedgerScoreRangeRule(0.2, 976.7),
            GlobalRedeliveryLedgerRequiredDepotRule("global redelivery ledger needs a value"),
            GlobalRedeliveryLedgerCodeLengthRule(120),
        ),
    )
}
