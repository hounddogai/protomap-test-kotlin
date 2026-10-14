package com.acme.transport.terminal.dwell.bundle

import com.acme.transport.kit.text.normalize

/** A rule a dwell agreement must pass. Check returns its problems, or none. */
interface DwellAgreementRule {
    fun check(item: DwellAgreement): MutableList<String>
}

data class DwellAgreementRequiredNameRule(var message: String) : DwellAgreementRule {
    override fun check(item: DwellAgreement): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DwellAgreementLoadKgRangeRule(var min: Double, var max: Double) : DwellAgreementRule {
    override fun check(item: DwellAgreement): MutableList<String> {
        val value = item.loadKg
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("load kg $value is below $min")
        }
        if (value > max) {
            problems.add("load kg $value is above $max")
        }
        return problems
    }
}

data class DwellAgreementExpiresAtRangeRule(var min: Int, var max: Int) : DwellAgreementRule {
    override fun check(item: DwellAgreement): MutableList<String> {
        val value = item.expiresAt
        if (value < min || value > max) {
            return mutableListOf<String>("expires at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class DwellAgreementReviewerLengthRule(var maxLength: Int) : DwellAgreementRule {
    override fun check(item: DwellAgreement): MutableList<String> {
        val size = item.reviewer.length
        if (size > maxLength) {
            return mutableListOf<String>("reviewer is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DwellAgreementAllowedReviewerRule(var allowed: MutableList<String>) : DwellAgreementRule {
    override fun check(item: DwellAgreement): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.reviewer))) {
            return mutableListOf<String>("reviewer ${item.reviewer} is duplicated")
        }
        return mutableListOf<String>()
    }
}

data class DwellAgreementNameLengthRule(var maxLength: Int) : DwellAgreementRule {
    override fun check(item: DwellAgreement): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a dwell agreement. */
data class DwellAgreementValidator(var rules: MutableList<DwellAgreementRule>) {
    fun validate(item: DwellAgreement): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: DwellAgreement) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DwellAgreement>): MutableMap<String, MutableList<String>> {
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

fun defaultDwellAgreementValidator(): DwellAgreementValidator {
    return DwellAgreementValidator(
        mutableListOf<DwellAgreementRule>(
            DwellAgreementRequiredNameRule("dwell agreement needs a value"),
            DwellAgreementLoadKgRangeRule(6.6, 2421.2),
            DwellAgreementExpiresAtRangeRule(3600, 86400),
            DwellAgreementReviewerLengthRule(1000),
            DwellAgreementAllowedReviewerRule(mutableListOf<String>("tiered", "batched")),
            DwellAgreementNameLengthRule(80),
        ),
    )
}
