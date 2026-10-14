package com.acme.transport.crewing.qualification.rule

import com.acme.transport.kit.text.normalize

/** A rule a qualification batch must pass. Check returns its problems, or none. */
interface QualificationBatchRule {
    fun check(item: QualificationBatch): MutableList<String>
}

data class QualificationBatchCarrierLengthRule(var maxLength: Int) : QualificationBatchRule {
    override fun check(item: QualificationBatch): MutableList<String> {
        val size = item.carrier.length
        if (size > maxLength) {
            return mutableListOf<String>("carrier is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class QualificationBatchNameLengthRule(var maxLength: Int) : QualificationBatchRule {
    override fun check(item: QualificationBatch): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class QualificationBatchAllowedNameRule(var allowed: MutableList<String>) : QualificationBatchRule {
    override fun check(item: QualificationBatch): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.name))) {
            return mutableListOf<String>("name ${item.name} is above maximum")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a qualification batch. */
data class QualificationBatchValidator(var rules: MutableList<QualificationBatchRule>) {
    fun validate(item: QualificationBatch): MutableList<String> {
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

    fun requireValid(item: QualificationBatch) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<QualificationBatch>): MutableMap<String, MutableList<String>> {
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

fun defaultQualificationBatchValidator(): QualificationBatchValidator {
    return QualificationBatchValidator(
        mutableListOf<QualificationBatchRule>(
            QualificationBatchCarrierLengthRule(255),
            QualificationBatchNameLengthRule(3600),
            QualificationBatchAllowedNameRule(mutableListOf<String>("default")),
        ),
    )
}
