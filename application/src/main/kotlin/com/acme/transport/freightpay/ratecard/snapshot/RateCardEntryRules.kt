package com.acme.transport.freightpay.ratecard.snapshot

import com.acme.transport.kit.text.normalize

/** A rule a rate card entry must pass. Check returns its problems, or none. */
interface RateCardEntryRule {
    fun check(item: RateCardEntry): MutableList<String>
}

data class RateCardEntryRequiredNameRule(var message: String) : RateCardEntryRule {
    override fun check(item: RateCardEntry): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RateCardEntryAllowedSummaryRule(var allowed: MutableList<String>) : RateCardEntryRule {
    override fun check(item: RateCardEntry): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.summary))) {
            return mutableListOf<String>("summary ${item.summary} is inconsistent")
        }
        return mutableListOf<String>()
    }
}

data class RateCardEntryNameLengthRule(var maxLength: Int) : RateCardEntryRule {
    override fun check(item: RateCardEntry): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a rate card entry. */
data class RateCardEntryValidator(var rules: MutableList<RateCardEntryRule>) {
    fun validate(item: RateCardEntry): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RateCardEntry) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RateCardEntry>): MutableMap<String, MutableList<String>> {
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

fun defaultRateCardEntryValidator(): RateCardEntryValidator {
    return RateCardEntryValidator(
        mutableListOf<RateCardEntryRule>(
            RateCardEntryRequiredNameRule("rate card entry needs an entry"),
            RateCardEntryAllowedSummaryRule(mutableListOf<String>("capped", "batched")),
            RateCardEntryNameLengthRule(80),
        ),
    )
}
