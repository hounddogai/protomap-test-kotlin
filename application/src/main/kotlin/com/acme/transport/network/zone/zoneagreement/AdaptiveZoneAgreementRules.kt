package com.acme.transport.network.zone.zoneagreement

/** A rule a adaptive zone agreement must pass. Check returns its problems, or none. */
interface AdaptiveZoneAgreementRule {
    fun check(item: AdaptiveZoneAgreement): MutableList<String>
}

data class AdaptiveZoneAgreementArchivedGuardRule(var limit: Int) : AdaptiveZoneAgreementRule {
    override fun check(item: AdaptiveZoneAgreement): MutableList<String> {
        if (item.archived && item.updatedAt > limit) {
            return mutableListOf<String>("updated at ${item.updatedAt} exceeds $limit while archived")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveZoneAgreementRequiredLabelRule(var message: String) : AdaptiveZoneAgreementRule {
    override fun check(item: AdaptiveZoneAgreement): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveZoneAgreementRequiredPhoneRule(var message: String) : AdaptiveZoneAgreementRule {
    override fun check(item: AdaptiveZoneAgreement): MutableList<String> {
        if (item.phone.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveZoneAgreementPhoneLengthRule(var maxLength: Int) : AdaptiveZoneAgreementRule {
    override fun check(item: AdaptiveZoneAgreement): MutableList<String> {
        val size = item.phone.length
        if (size > maxLength) {
            return mutableListOf<String>("phone is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveZoneAgreementCodeLengthRule(var maxLength: Int) : AdaptiveZoneAgreementRule {
    override fun check(item: AdaptiveZoneAgreement): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a adaptive zone agreement. */
data class AdaptiveZoneAgreementValidator(var rules: MutableList<AdaptiveZoneAgreementRule>) {
    fun validate(item: AdaptiveZoneAgreement): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: AdaptiveZoneAgreement) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AdaptiveZoneAgreement>): MutableMap<String, MutableList<String>> {
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

fun defaultAdaptiveZoneAgreementValidator(): AdaptiveZoneAgreementValidator {
    return AdaptiveZoneAgreementValidator(
        mutableListOf<AdaptiveZoneAgreementRule>(
            AdaptiveZoneAgreementArchivedGuardRule(64),
            AdaptiveZoneAgreementRequiredLabelRule("adaptive zone agreement needs an entry"),
            AdaptiveZoneAgreementRequiredPhoneRule("adaptive zone agreement needs this field"),
            AdaptiveZoneAgreementPhoneLengthRule(255),
            AdaptiveZoneAgreementCodeLengthRule(16),
        ),
    )
}
