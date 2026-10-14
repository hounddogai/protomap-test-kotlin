package com.acme.transport.fleetops.utilization.budget

import com.acme.transport.kit.text.normalize

/** A rule a utilization agreement must pass. Check returns its problems, or none. */
interface UtilizationAgreementRule {
    fun check(item: UtilizationAgreement): MutableList<String>
}

data class UtilizationAgreementUnitLengthRule(var maxLength: Int) : UtilizationAgreementRule {
    override fun check(item: UtilizationAgreement): MutableList<String> {
        val size = item.unit.length
        if (size > maxLength) {
            return mutableListOf<String>("unit is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class UtilizationAgreementVersionRangeRule(var min: Int, var max: Int) : UtilizationAgreementRule {
    override fun check(item: UtilizationAgreement): MutableList<String> {
        val value = item.version
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("version $value is below $min")
        }
        if (value > max) {
            problems.add("version $value is above $max")
        }
        return problems
    }
}

data class UtilizationAgreementLoadKgRangeRule(var min: Double, var max: Double) : UtilizationAgreementRule {
    override fun check(item: UtilizationAgreement): MutableList<String> {
        val value = item.loadKg
        if (value < min || value > max) {
            return mutableListOf<String>("load kg $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class UtilizationAgreementAllowedCodeRule(var allowed: MutableList<String>) : UtilizationAgreementRule {
    override fun check(item: UtilizationAgreement): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is missing")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a utilization agreement. */
data class UtilizationAgreementValidator(var rules: MutableList<UtilizationAgreementRule>) {
    fun validate(item: UtilizationAgreement): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: UtilizationAgreement) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<UtilizationAgreement>): MutableMap<String, MutableList<String>> {
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

fun defaultUtilizationAgreementValidator(): UtilizationAgreementValidator {
    return UtilizationAgreementValidator(
        mutableListOf<UtilizationAgreementRule>(
            UtilizationAgreementUnitLengthRule(80),
            UtilizationAgreementVersionRangeRule(32, 32),
            UtilizationAgreementLoadKgRangeRule(10.0, 2775.3),
            UtilizationAgreementAllowedCodeRule(mutableListOf<String>("weighted", "adaptive", "tiered")),
        ),
    )
}
