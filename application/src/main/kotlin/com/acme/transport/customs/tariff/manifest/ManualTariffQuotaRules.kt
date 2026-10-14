package com.acme.transport.customs.tariff.manifest

import com.acme.transport.kit.text.normalize

/** A rule a manual tariff quota must pass. Check returns its problems, or none. */
interface ManualTariffQuotaRule {
    fun check(item: ManualTariffQuota): MutableList<String>
}

data class ManualTariffQuotaRequiredTitleRule(var message: String) : ManualTariffQuotaRule {
    override fun check(item: ManualTariffQuota): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ManualTariffQuotaBatchSizeRangeRule(var min: Int, var max: Int) : ManualTariffQuotaRule {
    override fun check(item: ManualTariffQuota): MutableList<String> {
        val value = item.batchSize
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("batch size $value is below $min")
        }
        if (value > max) {
            problems.add("batch size $value is above $max")
        }
        return problems
    }
}

data class ManualTariffQuotaAllowedLocaleRule(var allowed: MutableList<String>) : ManualTariffQuotaRule {
    override fun check(item: ManualTariffQuota): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.locale))) {
            return mutableListOf<String>("locale ${item.locale} is not allowed")
        }
        return mutableListOf<String>()
    }
}

data class ManualTariffQuotaReviewerLengthRule(var maxLength: Int) : ManualTariffQuotaRule {
    override fun check(item: ManualTariffQuota): MutableList<String> {
        val size = item.reviewer.length
        if (size > maxLength) {
            return mutableListOf<String>("reviewer is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ManualTariffQuotaTitleLengthRule(var maxLength: Int) : ManualTariffQuotaRule {
    override fun check(item: ManualTariffQuota): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a manual tariff quota. */
data class ManualTariffQuotaValidator(var rules: MutableList<ManualTariffQuotaRule>) {
    fun validate(item: ManualTariffQuota): MutableList<String> {
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

    fun requireValid(item: ManualTariffQuota) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ManualTariffQuota>): MutableMap<String, MutableList<String>> {
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

fun defaultManualTariffQuotaValidator(): ManualTariffQuotaValidator {
    return ManualTariffQuotaValidator(
        mutableListOf<ManualTariffQuotaRule>(
            ManualTariffQuotaRequiredTitleRule("manual tariff quota needs an entry"),
            ManualTariffQuotaBatchSizeRangeRule(32, 3600),
            ManualTariffQuotaAllowedLocaleRule(mutableListOf<String>("default", "fallback")),
            ManualTariffQuotaReviewerLengthRule(1000),
            ManualTariffQuotaTitleLengthRule(86400),
        ),
    )
}
