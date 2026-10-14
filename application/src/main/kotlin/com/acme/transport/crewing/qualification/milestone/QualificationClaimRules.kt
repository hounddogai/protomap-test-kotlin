package com.acme.transport.crewing.qualification.milestone

/** A rule a qualification claim must pass. Check returns its problems, or none. */
interface QualificationClaimRule {
    fun check(item: QualificationClaim): MutableList<String>
}

data class QualificationClaimCodeLengthRule(var maxLength: Int) : QualificationClaimRule {
    override fun check(item: QualificationClaim): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class QualificationClaimRateRangeRule(var min: Double, var max: Double) : QualificationClaimRule {
    override fun check(item: QualificationClaim): MutableList<String> {
        val value = item.rate
        if (value < min || value > max) {
            return mutableListOf<String>("rate $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class QualificationClaimCodePrefixRule(var prefix: String) : QualificationClaimRule {
    override fun check(item: QualificationClaim): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a qualification claim. */
data class QualificationClaimValidator(var rules: MutableList<QualificationClaimRule>) {
    fun validate(item: QualificationClaim): MutableList<String> {
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

    fun requireValid(item: QualificationClaim) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<QualificationClaim>): MutableMap<String, MutableList<String>> {
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

fun defaultQualificationClaimValidator(): QualificationClaimValidator {
    return QualificationClaimValidator(
        mutableListOf<QualificationClaimRule>(
            QualificationClaimCodeLengthRule(32),
            QualificationClaimRateRangeRule(9.4, 1136.2),
            QualificationClaimCodePrefixRule("X"),
        ),
    )
}
