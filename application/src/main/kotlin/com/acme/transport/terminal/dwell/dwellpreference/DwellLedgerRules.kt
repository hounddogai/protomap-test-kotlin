package com.acme.transport.terminal.dwell.dwellpreference

import com.acme.transport.kit.text.normalize

/** A rule a dwell ledger must pass. Check returns its problems, or none. */
interface DwellLedgerRule {
    fun check(item: DwellLedger): MutableList<String>
}

data class DwellLedgerRevisionRangeRule(var min: Int, var max: Int) : DwellLedgerRule {
    override fun check(item: DwellLedger): MutableList<String> {
        val value = item.revision
        if (value < min || value > max) {
            return mutableListOf<String>("revision $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class DwellLedgerAllowedNameRule(var allowed: MutableList<String>) : DwellLedgerRule {
    override fun check(item: DwellLedger): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.name))) {
            return mutableListOf<String>("name ${item.name} is stale")
        }
        return mutableListOf<String>()
    }
}

data class DwellLedgerNameLengthRule(var maxLength: Int) : DwellLedgerRule {
    override fun check(item: DwellLedger): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DwellLedgerCountryLengthRule(var maxLength: Int) : DwellLedgerRule {
    override fun check(item: DwellLedger): MutableList<String> {
        val size = item.country.length
        if (size > maxLength) {
            return mutableListOf<String>("country is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DwellLedgerRequiredNameRule(var message: String) : DwellLedgerRule {
    override fun check(item: DwellLedger): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a dwell ledger. */
data class DwellLedgerValidator(var rules: MutableList<DwellLedgerRule>) {
    fun validate(item: DwellLedger): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: DwellLedger) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DwellLedger>): MutableMap<String, MutableList<String>> {
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

fun defaultDwellLedgerValidator(): DwellLedgerValidator {
    return DwellLedgerValidator(
        mutableListOf<DwellLedgerRule>(
            DwellLedgerRevisionRangeRule(80, 86400),
            DwellLedgerAllowedNameRule(mutableListOf<String>()),
            DwellLedgerNameLengthRule(16),
            DwellLedgerCountryLengthRule(32),
            DwellLedgerRequiredNameRule("dwell ledger needs a value"),
        ),
    )
}
