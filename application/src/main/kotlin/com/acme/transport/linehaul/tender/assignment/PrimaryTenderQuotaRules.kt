package com.acme.transport.linehaul.tender.assignment

/** A rule a primary tender quota must pass. Check returns its problems, or none. */
interface PrimaryTenderQuotaRule {
    fun check(item: PrimaryTenderQuota): MutableList<String>
}

data class PrimaryTenderQuotaRequiredTitleRule(var message: String) : PrimaryTenderQuotaRule {
    override fun check(item: PrimaryTenderQuota): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PrimaryTenderQuotaPhoneLengthRule(var maxLength: Int) : PrimaryTenderQuotaRule {
    override fun check(item: PrimaryTenderQuota): MutableList<String> {
        val size = item.phone.length
        if (size > maxLength) {
            return mutableListOf<String>("phone is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryTenderQuotaRetriesRangeRule(var min: Int, var max: Int) : PrimaryTenderQuotaRule {
    override fun check(item: PrimaryTenderQuota): MutableList<String> {
        val value = item.retries
        if (value < min || value > max) {
            return mutableListOf<String>("retries $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryTenderQuotaUpdatedAtRangeRule(var min: Int, var max: Int) : PrimaryTenderQuotaRule {
    override fun check(item: PrimaryTenderQuota): MutableList<String> {
        val value = item.updatedAt
        if (value < min || value > max) {
            return mutableListOf<String>("updated at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a primary tender quota. */
data class PrimaryTenderQuotaValidator(var rules: MutableList<PrimaryTenderQuotaRule>) {
    fun validate(item: PrimaryTenderQuota): MutableList<String> {
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

    fun requireValid(item: PrimaryTenderQuota) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PrimaryTenderQuota>): MutableMap<String, MutableList<String>> {
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

fun defaultPrimaryTenderQuotaValidator(): PrimaryTenderQuotaValidator {
    return PrimaryTenderQuotaValidator(
        mutableListOf<PrimaryTenderQuotaRule>(
            PrimaryTenderQuotaRequiredTitleRule("primary tender quota needs an entry"),
            PrimaryTenderQuotaPhoneLengthRule(1000),
            PrimaryTenderQuotaRetriesRangeRule(16, 16),
            PrimaryTenderQuotaUpdatedAtRangeRule(32, 255),
        ),
    )
}
