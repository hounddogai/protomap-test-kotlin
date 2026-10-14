package com.acme.transport.freightpay.ratecard.agreement

import com.acme.transport.kit.text.normalize

/** A rule a pending rate card agreement must pass. Check returns its problems, or none. */
interface PendingRateCardAgreementRule {
    fun check(item: PendingRateCardAgreement): MutableList<String>
}

data class PendingRateCardAgreementRequiredLabelRule(var message: String) : PendingRateCardAgreementRule {
    override fun check(item: PendingRateCardAgreement): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PendingRateCardAgreementCodeLengthRule(var maxLength: Int) : PendingRateCardAgreementRule {
    override fun check(item: PendingRateCardAgreement): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PendingRateCardAgreementLabelLengthRule(var maxLength: Int) : PendingRateCardAgreementRule {
    override fun check(item: PendingRateCardAgreement): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PendingRateCardAgreementAllowedLabelRule(
    var allowed: MutableList<String>,
) : PendingRateCardAgreementRule {
    override fun check(item: PendingRateCardAgreement): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.label))) {
            return mutableListOf<String>("label ${item.label} is above maximum")
        }
        return mutableListOf<String>()
    }
}

data class PendingRateCardAgreementRequiredCodeRule(var message: String) : PendingRateCardAgreementRule {
    override fun check(item: PendingRateCardAgreement): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a pending rate card agreement. */
data class PendingRateCardAgreementValidator(var rules: MutableList<PendingRateCardAgreementRule>) {
    fun validate(item: PendingRateCardAgreement): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: PendingRateCardAgreement) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PendingRateCardAgreement>): MutableMap<String, MutableList<String>> {
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

fun defaultPendingRateCardAgreementValidator(): PendingRateCardAgreementValidator {
    return PendingRateCardAgreementValidator(
        mutableListOf<PendingRateCardAgreementRule>(
            PendingRateCardAgreementRequiredLabelRule("pending rate card agreement needs a value"),
            PendingRateCardAgreementCodeLengthRule(32),
            PendingRateCardAgreementLabelLengthRule(255),
            PendingRateCardAgreementAllowedLabelRule(mutableListOf<String>("rolling", "manual", "batched")),
            PendingRateCardAgreementRequiredCodeRule("pending rate card agreement needs an entry"),
        ),
    )
}
