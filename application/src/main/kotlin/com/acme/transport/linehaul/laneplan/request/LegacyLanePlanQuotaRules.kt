package com.acme.transport.linehaul.laneplan.request

/** A rule a legacy lane plan quota must pass. Check returns its problems, or none. */
interface LegacyLanePlanQuotaRule {
    fun check(item: LegacyLanePlanQuota): MutableList<String>
}

data class LegacyLanePlanQuotaPhoneLengthRule(var maxLength: Int) : LegacyLanePlanQuotaRule {
    override fun check(item: LegacyLanePlanQuota): MutableList<String> {
        val size = item.phone.length
        if (size > maxLength) {
            return mutableListOf<String>("phone is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LegacyLanePlanQuotaRequiredSegmentRule(var message: String) : LegacyLanePlanQuotaRule {
    override fun check(item: LegacyLanePlanQuota): MutableList<String> {
        if (item.segment.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LegacyLanePlanQuotaSegmentLengthRule(var maxLength: Int) : LegacyLanePlanQuotaRule {
    override fun check(item: LegacyLanePlanQuota): MutableList<String> {
        val size = item.segment.length
        if (size > maxLength) {
            return mutableListOf<String>("segment is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a legacy lane plan quota. */
data class LegacyLanePlanQuotaValidator(var rules: MutableList<LegacyLanePlanQuotaRule>) {
    fun validate(item: LegacyLanePlanQuota): MutableList<String> {
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

    fun requireValid(item: LegacyLanePlanQuota) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LegacyLanePlanQuota>): MutableMap<String, MutableList<String>> {
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

fun defaultLegacyLanePlanQuotaValidator(): LegacyLanePlanQuotaValidator {
    return LegacyLanePlanQuotaValidator(
        mutableListOf<LegacyLanePlanQuotaRule>(
            LegacyLanePlanQuotaPhoneLengthRule(86400),
            LegacyLanePlanQuotaRequiredSegmentRule("legacy lane plan quota needs an entry"),
            LegacyLanePlanQuotaSegmentLengthRule(32),
        ),
    )
}
