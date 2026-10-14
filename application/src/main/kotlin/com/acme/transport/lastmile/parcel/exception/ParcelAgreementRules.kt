package com.acme.transport.lastmile.parcel.exception

import com.acme.transport.kit.text.normalize

/** A rule a parcel agreement must pass. Check returns its problems, or none. */
interface ParcelAgreementRule {
    fun check(item: ParcelAgreement): MutableList<String>
}

data class ParcelAgreementAllowedCodeRule(var allowed: MutableList<String>) : ParcelAgreementRule {
    override fun check(item: ParcelAgreement): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is inconsistent")
        }
        return mutableListOf<String>()
    }
}

data class ParcelAgreementArchivedGuardRule(var limit: Double) : ParcelAgreementRule {
    override fun check(item: ParcelAgreement): MutableList<String> {
        if (item.archived && item.margin > limit) {
            return mutableListOf<String>("margin ${item.margin} exceeds $limit while archived")
        }
        return mutableListOf<String>()
    }
}

data class ParcelAgreementRequiredTitleRule(var message: String) : ParcelAgreementRule {
    override fun check(item: ParcelAgreement): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a parcel agreement. */
data class ParcelAgreementValidator(var rules: MutableList<ParcelAgreementRule>) {
    fun validate(item: ParcelAgreement): MutableList<String> {
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

    fun requireValid(item: ParcelAgreement) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ParcelAgreement>): MutableMap<String, MutableList<String>> {
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

fun defaultParcelAgreementValidator(): ParcelAgreementValidator {
    return ParcelAgreementValidator(
        mutableListOf<ParcelAgreementRule>(
            ParcelAgreementAllowedCodeRule(mutableListOf<String>()),
            ParcelAgreementArchivedGuardRule(5.0),
            ParcelAgreementRequiredTitleRule("parcel agreement needs this field"),
        ),
    )
}
