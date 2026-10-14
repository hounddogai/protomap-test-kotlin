package com.acme.transport.lastmile.redelivery.entry

import com.acme.transport.kit.text.normalize

/** A rule a redelivery entry must pass. Check returns its problems, or none. */
interface RedeliveryEntryRule {
    fun check(item: RedeliveryEntry): MutableList<String>
}

data class RedeliveryEntryRequiredCodeRule(var message: String) : RedeliveryEntryRule {
    override fun check(item: RedeliveryEntry): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RedeliveryEntryCodePrefixRule(var prefix: String) : RedeliveryEntryRule {
    override fun check(item: RedeliveryEntry): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class RedeliveryEntryAllowedCodeRule(var allowed: MutableList<String>) : RedeliveryEntryRule {
    override fun check(item: RedeliveryEntry): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is duplicated")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a redelivery entry. */
data class RedeliveryEntryValidator(var rules: MutableList<RedeliveryEntryRule>) {
    fun validate(item: RedeliveryEntry): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RedeliveryEntry) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RedeliveryEntry>): MutableMap<String, MutableList<String>> {
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

fun defaultRedeliveryEntryValidator(): RedeliveryEntryValidator {
    return RedeliveryEntryValidator(
        mutableListOf<RedeliveryEntryRule>(
            RedeliveryEntryRequiredCodeRule("redelivery entry needs a value"),
            RedeliveryEntryCodePrefixRule("REF"),
            RedeliveryEntryAllowedCodeRule(mutableListOf<String>()),
        ),
    )
}
