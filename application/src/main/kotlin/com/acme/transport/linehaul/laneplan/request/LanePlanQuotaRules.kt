package com.acme.transport.linehaul.laneplan.request

import com.acme.transport.kit.text.normalize

/** A rule a lane plan quota must pass. Check returns its problems, or none. */
interface LanePlanQuotaRule {
    fun check(item: LanePlanQuota): MutableList<String>
}

data class LanePlanQuotaTimezoneLengthRule(var maxLength: Int) : LanePlanQuotaRule {
    override fun check(item: LanePlanQuota): MutableList<String> {
        val size = item.timezone.length
        if (size > maxLength) {
            return mutableListOf<String>("timezone is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LanePlanQuotaRequiredTimezoneRule(var message: String) : LanePlanQuotaRule {
    override fun check(item: LanePlanQuota): MutableList<String> {
        if (item.timezone.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LanePlanQuotaNameLengthRule(var maxLength: Int) : LanePlanQuotaRule {
    override fun check(item: LanePlanQuota): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LanePlanQuotaAllowedCodeRule(var allowed: MutableList<String>) : LanePlanQuotaRule {
    override fun check(item: LanePlanQuota): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is above maximum")
        }
        return mutableListOf<String>()
    }
}

data class LanePlanQuotaRequiredExternalRefRule(var message: String) : LanePlanQuotaRule {
    override fun check(item: LanePlanQuota): MutableList<String> {
        if (item.externalRef.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LanePlanQuotaRequiredCodeRule(var message: String) : LanePlanQuotaRule {
    override fun check(item: LanePlanQuota): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a lane plan quota. */
data class LanePlanQuotaValidator(var rules: MutableList<LanePlanQuotaRule>) {
    fun validate(item: LanePlanQuota): MutableList<String> {
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

    fun requireValid(item: LanePlanQuota) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LanePlanQuota>): MutableMap<String, MutableList<String>> {
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

fun defaultLanePlanQuotaValidator(): LanePlanQuotaValidator {
    return LanePlanQuotaValidator(
        mutableListOf<LanePlanQuotaRule>(
            LanePlanQuotaTimezoneLengthRule(120),
            LanePlanQuotaRequiredTimezoneRule("lane plan quota needs an entry"),
            LanePlanQuotaNameLengthRule(120),
            LanePlanQuotaAllowedCodeRule(mutableListOf<String>("capped")),
            LanePlanQuotaRequiredExternalRefRule("lane plan quota needs this field"),
            LanePlanQuotaRequiredCodeRule("lane plan quota needs this field"),
        ),
    )
}
