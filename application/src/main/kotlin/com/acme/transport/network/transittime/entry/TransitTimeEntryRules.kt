package com.acme.transport.network.transittime.entry

import com.acme.transport.kit.text.normalize

/** A rule a transit time entry must pass. Check returns its problems, or none. */
interface TransitTimeEntryRule {
    fun check(item: TransitTimeEntry): MutableList<String>
}

data class TransitTimeEntryAllowedCodeRule(var allowed: MutableList<String>) : TransitTimeEntryRule {
    override fun check(item: TransitTimeEntry): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is unknown")
        }
        return mutableListOf<String>()
    }
}

data class TransitTimeEntryRequiredCodeRule(var message: String) : TransitTimeEntryRule {
    override fun check(item: TransitTimeEntry): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TransitTimeEntryCodeLengthRule(var maxLength: Int) : TransitTimeEntryRule {
    override fun check(item: TransitTimeEntry): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TransitTimeEntryTitleLengthRule(var maxLength: Int) : TransitTimeEntryRule {
    override fun check(item: TransitTimeEntry): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a transit time entry. */
data class TransitTimeEntryValidator(var rules: MutableList<TransitTimeEntryRule>) {
    fun validate(item: TransitTimeEntry): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: TransitTimeEntry) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TransitTimeEntry>): MutableMap<String, MutableList<String>> {
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

fun defaultTransitTimeEntryValidator(): TransitTimeEntryValidator {
    return TransitTimeEntryValidator(
        mutableListOf<TransitTimeEntryRule>(
            TransitTimeEntryAllowedCodeRule(mutableListOf<String>("global")),
            TransitTimeEntryRequiredCodeRule("transit time entry needs a value"),
            TransitTimeEntryCodeLengthRule(86400),
            TransitTimeEntryTitleLengthRule(86400),
        ),
    )
}
