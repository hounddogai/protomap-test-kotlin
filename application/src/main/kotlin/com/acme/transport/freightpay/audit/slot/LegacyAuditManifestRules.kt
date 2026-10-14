package com.acme.transport.freightpay.audit.slot

import com.acme.transport.kit.text.normalize

/** A rule a legacy audit manifest must pass. Check returns its problems, or none. */
interface LegacyAuditManifestRule {
    fun check(item: LegacyAuditManifest): MutableList<String>
}

data class LegacyAuditManifestAllowedCodeRule(var allowed: MutableList<String>) : LegacyAuditManifestRule {
    override fun check(item: LegacyAuditManifest): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is not allowed")
        }
        return mutableListOf<String>()
    }
}

data class LegacyAuditManifestLabelLengthRule(var maxLength: Int) : LegacyAuditManifestRule {
    override fun check(item: LegacyAuditManifest): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LegacyAuditManifestDescriptionLengthRule(var maxLength: Int) : LegacyAuditManifestRule {
    override fun check(item: LegacyAuditManifest): MutableList<String> {
        val size = item.description.length
        if (size > maxLength) {
            return mutableListOf<String>("description is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a legacy audit manifest. */
data class LegacyAuditManifestValidator(var rules: MutableList<LegacyAuditManifestRule>) {
    fun validate(item: LegacyAuditManifest): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: LegacyAuditManifest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LegacyAuditManifest>): MutableMap<String, MutableList<String>> {
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

fun defaultLegacyAuditManifestValidator(): LegacyAuditManifestValidator {
    return LegacyAuditManifestValidator(
        mutableListOf<LegacyAuditManifestRule>(
            LegacyAuditManifestAllowedCodeRule(mutableListOf<String>()),
            LegacyAuditManifestLabelLengthRule(1000),
            LegacyAuditManifestDescriptionLengthRule(86400),
        ),
    )
}
