package com.acme.transport.crewing.qualification.cycle

import com.acme.transport.kit.text.normalize

/** A rule a qualification variance must pass. Check returns its problems, or none. */
interface QualificationVarianceRule {
    fun check(item: QualificationVariance): MutableList<String>
}

data class QualificationVarianceAllowedCodeRule(var allowed: MutableList<String>) : QualificationVarianceRule {
    override fun check(item: QualificationVariance): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is not allowed")
        }
        return mutableListOf<String>()
    }
}

data class QualificationVarianceRequiredExternalRefRule(var message: String) : QualificationVarianceRule {
    override fun check(item: QualificationVariance): MutableList<String> {
        if (item.externalRef.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class QualificationVarianceRequiredChecksumRule(var message: String) : QualificationVarianceRule {
    override fun check(item: QualificationVariance): MutableList<String> {
        if (item.checksum.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a qualification variance. */
data class QualificationVarianceValidator(var rules: MutableList<QualificationVarianceRule>) {
    fun validate(item: QualificationVariance): MutableList<String> {
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

    fun requireValid(item: QualificationVariance) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<QualificationVariance>): MutableMap<String, MutableList<String>> {
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

fun defaultQualificationVarianceValidator(): QualificationVarianceValidator {
    return QualificationVarianceValidator(
        mutableListOf<QualificationVarianceRule>(
            QualificationVarianceAllowedCodeRule(mutableListOf<String>()),
            QualificationVarianceRequiredExternalRefRule("qualification variance needs a value"),
            QualificationVarianceRequiredChecksumRule("qualification variance needs an entry"),
        ),
    )
}
