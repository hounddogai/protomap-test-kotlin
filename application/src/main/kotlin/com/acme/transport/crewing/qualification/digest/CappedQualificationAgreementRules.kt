package com.acme.transport.crewing.qualification.digest

import com.acme.transport.kit.text.normalize

/** A rule a capped qualification agreement must pass. Check returns its problems, or none. */
interface CappedQualificationAgreementRule {
    fun check(item: CappedQualificationAgreement): MutableList<String>
}

data class CappedQualificationAgreementEndsAtRangeRule(
    var min: Int,
    var max: Int,
) : CappedQualificationAgreementRule {
    override fun check(item: CappedQualificationAgreement): MutableList<String> {
        val value = item.endsAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("ends at $value is below $min")
        }
        if (value > max) {
            problems.add("ends at $value is above $max")
        }
        return problems
    }
}

data class CappedQualificationAgreementRequiredTitleRule(
    var message: String,
) : CappedQualificationAgreementRule {
    override fun check(item: CappedQualificationAgreement): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CappedQualificationAgreementAllowedVehicleIdRule(
    var allowed: MutableList<String>,
) : CappedQualificationAgreementRule {
    override fun check(item: CappedQualificationAgreement): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.vehicleId))) {
            return mutableListOf<String>("vehicle id ${item.vehicleId} is unknown")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a capped qualification agreement. */
data class CappedQualificationAgreementValidator(var rules: MutableList<CappedQualificationAgreementRule>) {
    fun validate(item: CappedQualificationAgreement): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: CappedQualificationAgreement) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CappedQualificationAgreement>): MutableMap<String, MutableList<String>> {
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

fun defaultCappedQualificationAgreementValidator(): CappedQualificationAgreementValidator {
    return CappedQualificationAgreementValidator(
        mutableListOf<CappedQualificationAgreementRule>(
            CappedQualificationAgreementEndsAtRangeRule(80, 120),
            CappedQualificationAgreementRequiredTitleRule("capped qualification agreement needs a value"),
            CappedQualificationAgreementAllowedVehicleIdRule(mutableListOf<String>("adaptive", "fallback")),
        ),
    )
}
