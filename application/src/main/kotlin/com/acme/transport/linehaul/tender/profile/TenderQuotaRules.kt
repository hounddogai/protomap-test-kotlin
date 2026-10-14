package com.acme.transport.linehaul.tender.profile

import com.acme.transport.kit.text.normalize

/** A rule a tender quota must pass. Check returns its problems, or none. */
interface TenderQuotaRule {
    fun check(item: TenderQuota): MutableList<String>
}

data class TenderQuotaRequiredLabelRule(var message: String) : TenderQuotaRule {
    override fun check(item: TenderQuota): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TenderQuotaCodeLengthRule(var maxLength: Int) : TenderQuotaRule {
    override fun check(item: TenderQuota): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TenderQuotaAllowedLabelRule(var allowed: MutableList<String>) : TenderQuotaRule {
    override fun check(item: TenderQuota): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.label))) {
            return mutableListOf<String>("label ${item.label} is above maximum")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a tender quota. */
data class TenderQuotaValidator(var rules: MutableList<TenderQuotaRule>) {
    fun validate(item: TenderQuota): MutableList<String> {
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

    fun requireValid(item: TenderQuota) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TenderQuota>): MutableMap<String, MutableList<String>> {
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

fun defaultTenderQuotaValidator(): TenderQuotaValidator {
    return TenderQuotaValidator(
        mutableListOf<TenderQuotaRule>(
            TenderQuotaRequiredLabelRule("tender quota needs a value"),
            TenderQuotaCodeLengthRule(80),
            TenderQuotaAllowedLabelRule(mutableListOf<String>("legacy", "legacy")),
        ),
    )
}
