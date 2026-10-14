package com.acme.transport.crewing.qualification.target

import com.acme.transport.kit.text.normalize

/** A rule a qualification quota must pass. Check returns its problems, or none. */
interface QualificationQuotaRule {
    fun check(item: QualificationQuota): MutableList<String>
}

data class QualificationQuotaRequiredChecksumRule(var message: String) : QualificationQuotaRule {
    override fun check(item: QualificationQuota): MutableList<String> {
        if (item.checksum.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class QualificationQuotaChecksumLengthRule(var maxLength: Int) : QualificationQuotaRule {
    override fun check(item: QualificationQuota): MutableList<String> {
        val size = item.checksum.length
        if (size > maxLength) {
            return mutableListOf<String>("checksum is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class QualificationQuotaRequiredDockRule(var message: String) : QualificationQuotaRule {
    override fun check(item: QualificationQuota): MutableList<String> {
        if (item.dock.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class QualificationQuotaAllowedChecksumRule(var allowed: MutableList<String>) : QualificationQuotaRule {
    override fun check(item: QualificationQuota): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.checksum))) {
            return mutableListOf<String>("checksum ${item.checksum} is below minimum")
        }
        return mutableListOf<String>()
    }
}

data class QualificationQuotaCodeLengthRule(var maxLength: Int) : QualificationQuotaRule {
    override fun check(item: QualificationQuota): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a qualification quota. */
data class QualificationQuotaValidator(var rules: MutableList<QualificationQuotaRule>) {
    fun validate(item: QualificationQuota): MutableList<String> {
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

    fun requireValid(item: QualificationQuota) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<QualificationQuota>): MutableMap<String, MutableList<String>> {
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

fun defaultQualificationQuotaValidator(): QualificationQuotaValidator {
    return QualificationQuotaValidator(
        mutableListOf<QualificationQuotaRule>(
            QualificationQuotaRequiredChecksumRule("qualification quota needs an entry"),
            QualificationQuotaChecksumLengthRule(80),
            QualificationQuotaRequiredDockRule("qualification quota needs this field"),
            QualificationQuotaAllowedChecksumRule(mutableListOf<String>("legacy", "default")),
            QualificationQuotaCodeLengthRule(3600),
        ),
    )
}
