package com.acme.transport.linehaul.backhaul.regionalbackhaultarget

import com.acme.transport.kit.text.normalize

/** A rule a legacy backhaul quota must pass. Check returns its problems, or none. */
interface LegacyBackhaulQuotaRule {
    fun check(item: LegacyBackhaulQuota): MutableList<String>
}

data class LegacyBackhaulQuotaUnitLengthRule(var maxLength: Int) : LegacyBackhaulQuotaRule {
    override fun check(item: LegacyBackhaulQuota): MutableList<String> {
        val size = item.unit.length
        if (size > maxLength) {
            return mutableListOf<String>("unit is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LegacyBackhaulQuotaRequiredLabelRule(var message: String) : LegacyBackhaulQuotaRule {
    override fun check(item: LegacyBackhaulQuota): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LegacyBackhaulQuotaRequiredUnitRule(var message: String) : LegacyBackhaulQuotaRule {
    override fun check(item: LegacyBackhaulQuota): MutableList<String> {
        if (item.unit.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LegacyBackhaulQuotaAllowedCodeRule(var allowed: MutableList<String>) : LegacyBackhaulQuotaRule {
    override fun check(item: LegacyBackhaulQuota): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is below minimum")
        }
        return mutableListOf<String>()
    }
}

data class LegacyBackhaulQuotaRequiredCodeRule(var message: String) : LegacyBackhaulQuotaRule {
    override fun check(item: LegacyBackhaulQuota): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LegacyBackhaulQuotaCodeLengthRule(var maxLength: Int) : LegacyBackhaulQuotaRule {
    override fun check(item: LegacyBackhaulQuota): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a legacy backhaul quota. */
data class LegacyBackhaulQuotaValidator(var rules: MutableList<LegacyBackhaulQuotaRule>) {
    fun validate(item: LegacyBackhaulQuota): MutableList<String> {
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

    fun requireValid(item: LegacyBackhaulQuota) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LegacyBackhaulQuota>): MutableMap<String, MutableList<String>> {
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

fun defaultLegacyBackhaulQuotaValidator(): LegacyBackhaulQuotaValidator {
    return LegacyBackhaulQuotaValidator(
        mutableListOf<LegacyBackhaulQuotaRule>(
            LegacyBackhaulQuotaUnitLengthRule(1000),
            LegacyBackhaulQuotaRequiredLabelRule("legacy backhaul quota needs a value"),
            LegacyBackhaulQuotaRequiredUnitRule("legacy backhaul quota needs an entry"),
            LegacyBackhaulQuotaAllowedCodeRule(mutableListOf<String>("default")),
            LegacyBackhaulQuotaRequiredCodeRule("legacy backhaul quota needs this field"),
            LegacyBackhaulQuotaCodeLengthRule(16),
        ),
    )
}
