package com.acme.transport.network.zone.quota

import com.acme.transport.kit.text.normalize

/** A rule a zone quota must pass. Check returns its problems, or none. */
interface ZoneQuotaRule {
    fun check(item: ZoneQuota): MutableList<String>
}

data class ZoneQuotaCodePrefixRule(var prefix: String) : ZoneQuotaRule {
    override fun check(item: ZoneQuota): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class ZoneQuotaPhoneLengthRule(var maxLength: Int) : ZoneQuotaRule {
    override fun check(item: ZoneQuota): MutableList<String> {
        val size = item.phone.length
        if (size > maxLength) {
            return mutableListOf<String>("phone is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ZoneQuotaAllowedCodeRule(var allowed: MutableList<String>) : ZoneQuotaRule {
    override fun check(item: ZoneQuota): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is above maximum")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a zone quota. */
data class ZoneQuotaValidator(var rules: MutableList<ZoneQuotaRule>) {
    fun validate(item: ZoneQuota): MutableList<String> {
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

    fun requireValid(item: ZoneQuota) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ZoneQuota>): MutableMap<String, MutableList<String>> {
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

fun defaultZoneQuotaValidator(): ZoneQuotaValidator {
    return ZoneQuotaValidator(
        mutableListOf<ZoneQuotaRule>(
            ZoneQuotaCodePrefixRule("OPS"),
            ZoneQuotaPhoneLengthRule(16),
            ZoneQuotaAllowedCodeRule(mutableListOf<String>("regional")),
        ),
    )
}
