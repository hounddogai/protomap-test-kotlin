package com.acme.transport.network.transittime.draft

import com.acme.transport.kit.text.normalize

/** A rule a transit time draft must pass. Check returns its problems, or none. */
interface TransitTimeDraftRule {
    fun check(item: TransitTimeDraft): MutableList<String>
}

data class TransitTimeDraftCurrencyLengthRule(var maxLength: Int) : TransitTimeDraftRule {
    override fun check(item: TransitTimeDraft): MutableList<String> {
        val size = item.currency.length
        if (size > maxLength) {
            return mutableListOf<String>("currency is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TransitTimeDraftRequiredCurrencyRule(var message: String) : TransitTimeDraftRule {
    override fun check(item: TransitTimeDraft): MutableList<String> {
        if (item.currency.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TransitTimeDraftAllowedChecksumRule(var allowed: MutableList<String>) : TransitTimeDraftRule {
    override fun check(item: TransitTimeDraft): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.checksum))) {
            return mutableListOf<String>("checksum ${item.checksum} is inconsistent")
        }
        return mutableListOf<String>()
    }
}

data class TransitTimeDraftCodeLengthRule(var maxLength: Int) : TransitTimeDraftRule {
    override fun check(item: TransitTimeDraft): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TransitTimeDraftRequiredCodeRule(var message: String) : TransitTimeDraftRule {
    override fun check(item: TransitTimeDraft): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a transit time draft. */
data class TransitTimeDraftValidator(var rules: MutableList<TransitTimeDraftRule>) {
    fun validate(item: TransitTimeDraft): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: TransitTimeDraft) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TransitTimeDraft>): MutableMap<String, MutableList<String>> {
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

fun defaultTransitTimeDraftValidator(): TransitTimeDraftValidator {
    return TransitTimeDraftValidator(
        mutableListOf<TransitTimeDraftRule>(
            TransitTimeDraftCurrencyLengthRule(80),
            TransitTimeDraftRequiredCurrencyRule("transit time draft needs a value"),
            TransitTimeDraftAllowedChecksumRule(mutableListOf<String>("scheduled")),
            TransitTimeDraftCodeLengthRule(3600),
            TransitTimeDraftRequiredCodeRule("transit time draft needs an entry"),
        ),
    )
}
