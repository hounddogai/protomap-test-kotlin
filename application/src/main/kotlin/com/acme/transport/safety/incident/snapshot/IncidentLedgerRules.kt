package com.acme.transport.safety.incident.snapshot

/** A rule a incident ledger must pass. Check returns its problems, or none. */
interface IncidentLedgerRule {
    fun check(item: IncidentLedger): MutableList<String>
}

data class IncidentLedgerRequiredCodeRule(var message: String) : IncidentLedgerRule {
    override fun check(item: IncidentLedger): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class IncidentLedgerRequiredTitleRule(var message: String) : IncidentLedgerRule {
    override fun check(item: IncidentLedger): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class IncidentLedgerTitleLengthRule(var maxLength: Int) : IncidentLedgerRule {
    override fun check(item: IncidentLedger): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a incident ledger. */
data class IncidentLedgerValidator(var rules: MutableList<IncidentLedgerRule>) {
    fun validate(item: IncidentLedger): MutableList<String> {
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

    fun requireValid(item: IncidentLedger) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<IncidentLedger>): MutableMap<String, MutableList<String>> {
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

fun defaultIncidentLedgerValidator(): IncidentLedgerValidator {
    return IncidentLedgerValidator(
        mutableListOf<IncidentLedgerRule>(
            IncidentLedgerRequiredCodeRule("incident ledger needs a value"),
            IncidentLedgerRequiredTitleRule("incident ledger needs an entry"),
            IncidentLedgerTitleLengthRule(86400),
        ),
    )
}
