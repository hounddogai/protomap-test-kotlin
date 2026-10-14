package com.acme.transport.crewing.qualification.assignment

import com.acme.transport.kit.text.normalize

/** A rule a qualification agreement must pass. Check returns its problems, or none. */
interface QualificationAgreementRule {
    fun check(item: QualificationAgreement): MutableList<String>
}

data class QualificationAgreementRequiredCodeRule(var message: String) : QualificationAgreementRule {
    override fun check(item: QualificationAgreement): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class QualificationAgreementRequiredChecksumRule(var message: String) : QualificationAgreementRule {
    override fun check(item: QualificationAgreement): MutableList<String> {
        if (item.checksum.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class QualificationAgreementStartsAtRangeRule(var min: Int, var max: Int) : QualificationAgreementRule {
    override fun check(item: QualificationAgreement): MutableList<String> {
        val value = item.startsAt
        if (value < min || value > max) {
            return mutableListOf<String>("starts at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class QualificationAgreementAllowedChecksumRule(
    var allowed: MutableList<String>,
) : QualificationAgreementRule {
    override fun check(item: QualificationAgreement): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.checksum))) {
            return mutableListOf<String>("checksum ${item.checksum} is blocked")
        }
        return mutableListOf<String>()
    }
}

data class QualificationAgreementDiscountRangeRule(
    var min: Double,
    var max: Double,
) : QualificationAgreementRule {
    override fun check(item: QualificationAgreement): MutableList<String> {
        val value = item.discount
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("discount $value is below $min")
        }
        if (value > max) {
            problems.add("discount $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a qualification agreement. */
data class QualificationAgreementValidator(var rules: MutableList<QualificationAgreementRule>) {
    fun validate(item: QualificationAgreement): MutableList<String> {
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

    fun requireValid(item: QualificationAgreement) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<QualificationAgreement>): MutableMap<String, MutableList<String>> {
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

fun defaultQualificationAgreementValidator(): QualificationAgreementValidator {
    return QualificationAgreementValidator(
        mutableListOf<QualificationAgreementRule>(
            QualificationAgreementRequiredCodeRule("qualification agreement needs an entry"),
            QualificationAgreementRequiredChecksumRule("qualification agreement needs a value"),
            QualificationAgreementStartsAtRangeRule(80, 86400),
            QualificationAgreementAllowedChecksumRule(mutableListOf<String>("manual")),
            QualificationAgreementDiscountRangeRule(2.0, 4151.5),
        ),
    )
}
