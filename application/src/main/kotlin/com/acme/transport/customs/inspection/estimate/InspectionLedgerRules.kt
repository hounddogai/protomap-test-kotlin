package com.acme.transport.customs.inspection.estimate

import com.acme.transport.kit.text.normalize

/** A rule a inspection ledger must pass. Check returns its problems, or none. */
interface InspectionLedgerRule {
    fun check(item: InspectionLedger): MutableList<String>
}

data class InspectionLedgerRequiredNotesRule(var message: String) : InspectionLedgerRule {
    override fun check(item: InspectionLedger): MutableList<String> {
        if (item.notes.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class InspectionLedgerCodePrefixRule(var prefix: String) : InspectionLedgerRule {
    override fun check(item: InspectionLedger): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class InspectionLedgerRequiredNameRule(var message: String) : InspectionLedgerRule {
    override fun check(item: InspectionLedger): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class InspectionLedgerNotesLengthRule(var maxLength: Int) : InspectionLedgerRule {
    override fun check(item: InspectionLedger): MutableList<String> {
        val size = item.notes.length
        if (size > maxLength) {
            return mutableListOf<String>("notes is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class InspectionLedgerAllowedNotesRule(var allowed: MutableList<String>) : InspectionLedgerRule {
    override fun check(item: InspectionLedger): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.notes))) {
            return mutableListOf<String>("notes ${item.notes} is out of range")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a inspection ledger. */
data class InspectionLedgerValidator(var rules: MutableList<InspectionLedgerRule>) {
    fun validate(item: InspectionLedger): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: InspectionLedger) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<InspectionLedger>): MutableMap<String, MutableList<String>> {
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

fun defaultInspectionLedgerValidator(): InspectionLedgerValidator {
    return InspectionLedgerValidator(
        mutableListOf<InspectionLedgerRule>(
            InspectionLedgerRequiredNotesRule("inspection ledger needs a value"),
            InspectionLedgerCodePrefixRule("ACM"),
            InspectionLedgerRequiredNameRule("inspection ledger needs this field"),
            InspectionLedgerNotesLengthRule(86400),
            InspectionLedgerAllowedNotesRule(mutableListOf<String>("fallback", "default")),
        ),
    )
}
