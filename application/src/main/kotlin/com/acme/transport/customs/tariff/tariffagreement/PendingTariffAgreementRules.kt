package com.acme.transport.customs.tariff.tariffagreement

import com.acme.transport.kit.text.normalize

/** A rule a pending tariff agreement must pass. Check returns its problems, or none. */
interface PendingTariffAgreementRule {
    fun check(item: PendingTariffAgreement): MutableList<String>
}

data class PendingTariffAgreementAllowedSegmentRule(
    var allowed: MutableList<String>,
) : PendingTariffAgreementRule {
    override fun check(item: PendingTariffAgreement): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.segment))) {
            return mutableListOf<String>("segment ${item.segment} is unknown")
        }
        return mutableListOf<String>()
    }
}

data class PendingTariffAgreementRequiredSegmentRule(var message: String) : PendingTariffAgreementRule {
    override fun check(item: PendingTariffAgreement): MutableList<String> {
        if (item.segment.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PendingTariffAgreementCodeLengthRule(var maxLength: Int) : PendingTariffAgreementRule {
    override fun check(item: PendingTariffAgreement): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PendingTariffAgreementRequiredCodeRule(var message: String) : PendingTariffAgreementRule {
    override fun check(item: PendingTariffAgreement): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PendingTariffAgreementCodePrefixRule(var prefix: String) : PendingTariffAgreementRule {
    override fun check(item: PendingTariffAgreement): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a pending tariff agreement. */
data class PendingTariffAgreementValidator(var rules: MutableList<PendingTariffAgreementRule>) {
    fun validate(item: PendingTariffAgreement): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: PendingTariffAgreement) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PendingTariffAgreement>): MutableMap<String, MutableList<String>> {
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

fun defaultPendingTariffAgreementValidator(): PendingTariffAgreementValidator {
    return PendingTariffAgreementValidator(
        mutableListOf<PendingTariffAgreementRule>(
            PendingTariffAgreementAllowedSegmentRule(mutableListOf<String>("tiered", "primary", "scheduled")),
            PendingTariffAgreementRequiredSegmentRule("pending tariff agreement needs an entry"),
            PendingTariffAgreementCodeLengthRule(3600),
            PendingTariffAgreementRequiredCodeRule("pending tariff agreement needs an entry"),
            PendingTariffAgreementCodePrefixRule("Q"),
        ),
    )
}
