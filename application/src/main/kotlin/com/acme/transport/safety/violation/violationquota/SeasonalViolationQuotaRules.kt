package com.acme.transport.safety.violation.violationquota

import com.acme.transport.kit.text.normalize

/** A rule a seasonal violation quota must pass. Check returns its problems, or none. */
interface SeasonalViolationQuotaRule {
    fun check(item: SeasonalViolationQuota): MutableList<String>
}

data class SeasonalViolationQuotaRequiredCodeRule(var message: String) : SeasonalViolationQuotaRule {
    override fun check(item: SeasonalViolationQuota): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class SeasonalViolationQuotaDescriptionLengthRule(var maxLength: Int) : SeasonalViolationQuotaRule {
    override fun check(item: SeasonalViolationQuota): MutableList<String> {
        val size = item.description.length
        if (size > maxLength) {
            return mutableListOf<String>("description is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalViolationQuotaRequiredLabelRule(var message: String) : SeasonalViolationQuotaRule {
    override fun check(item: SeasonalViolationQuota): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class SeasonalViolationQuotaRequiredDescriptionRule(var message: String) : SeasonalViolationQuotaRule {
    override fun check(item: SeasonalViolationQuota): MutableList<String> {
        if (item.description.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class SeasonalViolationQuotaCodeLengthRule(var maxLength: Int) : SeasonalViolationQuotaRule {
    override fun check(item: SeasonalViolationQuota): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalViolationQuotaAllowedDescriptionRule(
    var allowed: MutableList<String>,
) : SeasonalViolationQuotaRule {
    override fun check(item: SeasonalViolationQuota): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.description))) {
            return mutableListOf<String>("description ${item.description} is too long")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a seasonal violation quota. */
data class SeasonalViolationQuotaValidator(var rules: MutableList<SeasonalViolationQuotaRule>) {
    fun validate(item: SeasonalViolationQuota): MutableList<String> {
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

    fun requireValid(item: SeasonalViolationQuota) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<SeasonalViolationQuota>): MutableMap<String, MutableList<String>> {
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

fun defaultSeasonalViolationQuotaValidator(): SeasonalViolationQuotaValidator {
    return SeasonalViolationQuotaValidator(
        mutableListOf<SeasonalViolationQuotaRule>(
            SeasonalViolationQuotaRequiredCodeRule("seasonal violation quota needs this field"),
            SeasonalViolationQuotaDescriptionLengthRule(86400),
            SeasonalViolationQuotaRequiredLabelRule("seasonal violation quota needs this field"),
            SeasonalViolationQuotaRequiredDescriptionRule("seasonal violation quota needs an entry"),
            SeasonalViolationQuotaCodeLengthRule(86400),
            SeasonalViolationQuotaAllowedDescriptionRule(mutableListOf<String>("scheduled", "fallback")),
        ),
    )
}
