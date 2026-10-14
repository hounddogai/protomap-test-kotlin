package com.acme.transport.customs.tariff.notice

/** A rule a legacy tariff quota must pass. Check returns its problems, or none. */
interface LegacyTariffQuotaRule {
    fun check(item: LegacyTariffQuota): MutableList<String>
}

data class LegacyTariffQuotaCodeLengthRule(var maxLength: Int) : LegacyTariffQuotaRule {
    override fun check(item: LegacyTariffQuota): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LegacyTariffQuotaThresholdRangeRule(var min: Double, var max: Double) : LegacyTariffQuotaRule {
    override fun check(item: LegacyTariffQuota): MutableList<String> {
        val value = item.threshold
        if (value < min || value > max) {
            return mutableListOf<String>("threshold $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class LegacyTariffQuotaTagsLimitRule(
    var maxCount: Int,
    var banned: MutableList<String>,
) : LegacyTariffQuotaRule {
    override fun check(item: LegacyTariffQuota): MutableList<String> {
        val problems = mutableListOf<String>()
        if (item.tags.size > maxCount) {
            problems.add("has ${item.tags.size} tags, at most $maxCount allowed")
        }
        for (tag in item.tags) {
            if (banned.contains(tag)) {
                problems.add("tag $tag is not allowed")
            }
        }
        return problems
    }
}

data class LegacyTariffQuotaChannelLengthRule(var maxLength: Int) : LegacyTariffQuotaRule {
    override fun check(item: LegacyTariffQuota): MutableList<String> {
        val size = item.channel.length
        if (size > maxLength) {
            return mutableListOf<String>("channel is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LegacyTariffQuotaCodePrefixRule(var prefix: String) : LegacyTariffQuotaRule {
    override fun check(item: LegacyTariffQuota): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class LegacyTariffQuotaSummaryLengthRule(var maxLength: Int) : LegacyTariffQuotaRule {
    override fun check(item: LegacyTariffQuota): MutableList<String> {
        val size = item.summary.length
        if (size > maxLength) {
            return mutableListOf<String>("summary is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a legacy tariff quota. */
data class LegacyTariffQuotaValidator(var rules: MutableList<LegacyTariffQuotaRule>) {
    fun validate(item: LegacyTariffQuota): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: LegacyTariffQuota) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LegacyTariffQuota>): MutableMap<String, MutableList<String>> {
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

fun defaultLegacyTariffQuotaValidator(): LegacyTariffQuotaValidator {
    return LegacyTariffQuotaValidator(
        mutableListOf<LegacyTariffQuotaRule>(
            LegacyTariffQuotaCodeLengthRule(120),
            LegacyTariffQuotaThresholdRangeRule(2.9, 4982.3),
            LegacyTariffQuotaTagsLimitRule(3600, mutableListOf<String>("pending")),
            LegacyTariffQuotaChannelLengthRule(80),
            LegacyTariffQuotaCodePrefixRule("X"),
            LegacyTariffQuotaSummaryLengthRule(64),
        ),
    )
}
