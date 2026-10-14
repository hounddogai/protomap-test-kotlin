package com.acme.transport.crewing.qualification.bundle

import com.acme.transport.kit.text.normalize

/** A rule a qualification manifest must pass. Check returns its problems, or none. */
interface QualificationManifestRule {
    fun check(item: QualificationManifest): MutableList<String>
}

data class QualificationManifestAllowedCodeRule(var allowed: MutableList<String>) : QualificationManifestRule {
    override fun check(item: QualificationManifest): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is blocked")
        }
        return mutableListOf<String>()
    }
}

data class QualificationManifestCodePrefixRule(var prefix: String) : QualificationManifestRule {
    override fun check(item: QualificationManifest): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class QualificationManifestRequiredSegmentRule(var message: String) : QualificationManifestRule {
    override fun check(item: QualificationManifest): MutableList<String> {
        if (item.segment.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class QualificationManifestCodeLengthRule(var maxLength: Int) : QualificationManifestRule {
    override fun check(item: QualificationManifest): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a qualification manifest. */
data class QualificationManifestValidator(var rules: MutableList<QualificationManifestRule>) {
    fun validate(item: QualificationManifest): MutableList<String> {
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

    fun requireValid(item: QualificationManifest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<QualificationManifest>): MutableMap<String, MutableList<String>> {
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

fun defaultQualificationManifestValidator(): QualificationManifestValidator {
    return QualificationManifestValidator(
        mutableListOf<QualificationManifestRule>(
            QualificationManifestAllowedCodeRule(mutableListOf<String>("tiered", "global", "pending")),
            QualificationManifestCodePrefixRule("X"),
            QualificationManifestRequiredSegmentRule("qualification manifest needs a value"),
            QualificationManifestCodeLengthRule(64),
        ),
    )
}
