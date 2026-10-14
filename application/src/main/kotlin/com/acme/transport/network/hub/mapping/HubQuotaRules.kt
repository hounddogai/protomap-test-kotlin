package com.acme.transport.network.hub.mapping

import com.acme.transport.kit.text.normalize

/** A rule a hub quota must pass. Check returns its problems, or none. */
interface HubQuotaRule {
    fun check(item: HubQuota): MutableList<String>
}

data class HubQuotaChecksumLengthRule(var maxLength: Int) : HubQuotaRule {
    override fun check(item: HubQuota): MutableList<String> {
        val size = item.checksum.length
        if (size > maxLength) {
            return mutableListOf<String>("checksum is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class HubQuotaAllowedTimezoneRule(var allowed: MutableList<String>) : HubQuotaRule {
    override fun check(item: HubQuota): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.timezone))) {
            return mutableListOf<String>("timezone ${item.timezone} is below minimum")
        }
        return mutableListOf<String>()
    }
}

data class HubQuotaActiveGuardRule(var limit: Int) : HubQuotaRule {
    override fun check(item: HubQuota): MutableList<String> {
        if (item.active && item.endsAt > limit) {
            return mutableListOf<String>("ends at ${item.endsAt} exceeds $limit while active")
        }
        return mutableListOf<String>()
    }
}

data class HubQuotaCodeLengthRule(var maxLength: Int) : HubQuotaRule {
    override fun check(item: HubQuota): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class HubQuotaRequiredNameRule(var message: String) : HubQuotaRule {
    override fun check(item: HubQuota): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a hub quota. */
data class HubQuotaValidator(var rules: MutableList<HubQuotaRule>) {
    fun validate(item: HubQuota): MutableList<String> {
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

    fun requireValid(item: HubQuota) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<HubQuota>): MutableMap<String, MutableList<String>> {
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

fun defaultHubQuotaValidator(): HubQuotaValidator {
    return HubQuotaValidator(
        mutableListOf<HubQuotaRule>(
            HubQuotaChecksumLengthRule(1000),
            HubQuotaAllowedTimezoneRule(mutableListOf<String>("pending", "tiered", "adaptive")),
            HubQuotaActiveGuardRule(1000),
            HubQuotaCodeLengthRule(32),
            HubQuotaRequiredNameRule("hub quota needs this field"),
        ),
    )
}
