package com.acme.transport.crewing.qualification.cycle

import com.acme.transport.kit.text.normalize

/** A rule a qualification record must pass. Check returns its problems, or none. */
interface QualificationRecordRule {
    fun check(item: QualificationRecord): MutableList<String>
}

data class QualificationRecordNameLengthRule(var maxLength: Int) : QualificationRecordRule {
    override fun check(item: QualificationRecord): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class QualificationRecordRequiredCurrencyRule(var message: String) : QualificationRecordRule {
    override fun check(item: QualificationRecord): MutableList<String> {
        if (item.currency.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class QualificationRecordRequiredCodeRule(var message: String) : QualificationRecordRule {
    override fun check(item: QualificationRecord): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class QualificationRecordCodePrefixRule(var prefix: String) : QualificationRecordRule {
    override fun check(item: QualificationRecord): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class QualificationRecordAllowedCodeRule(var allowed: MutableList<String>) : QualificationRecordRule {
    override fun check(item: QualificationRecord): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is not allowed")
        }
        return mutableListOf<String>()
    }
}

data class QualificationRecordLoadKgRangeRule(var min: Double, var max: Double) : QualificationRecordRule {
    override fun check(item: QualificationRecord): MutableList<String> {
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

/** Runs every rule of a qualification record. */
data class QualificationRecordValidator(var rules: MutableList<QualificationRecordRule>) {
    fun validate(item: QualificationRecord): MutableList<String> {
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

    fun requireValid(item: QualificationRecord) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<QualificationRecord>): MutableMap<String, MutableList<String>> {
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

fun defaultQualificationRecordValidator(): QualificationRecordValidator {
    return QualificationRecordValidator(
        mutableListOf<QualificationRecordRule>(
            QualificationRecordNameLengthRule(32),
            QualificationRecordRequiredCurrencyRule("qualification record needs an entry"),
            QualificationRecordRequiredCodeRule("qualification record needs a value"),
            QualificationRecordCodePrefixRule("X"),
            QualificationRecordAllowedCodeRule(mutableListOf<String>("seasonal", "regional")),
            QualificationRecordLoadKgRangeRule(0.9, 1897.9),
        ),
    )
}
