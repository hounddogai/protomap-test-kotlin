package com.acme.transport.freightpay.dispute.group

/** A rule a dispute quota must pass. Check returns its problems, or none. */
interface DisputeQuotaRule {
    fun check(item: DisputeQuota): MutableList<String>
}

data class DisputeQuotaTitleLengthRule(var maxLength: Int) : DisputeQuotaRule {
    override fun check(item: DisputeQuota): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DisputeQuotaRateRangeRule(var min: Double, var max: Double) : DisputeQuotaRule {
    override fun check(item: DisputeQuota): MutableList<String> {
        val value = item.rate
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("rate $value is below $min")
        }
        if (value > max) {
            problems.add("rate $value is above $max")
        }
        return problems
    }
}

data class DisputeQuotaPinnedGuardRule(var limit: Int) : DisputeQuotaRule {
    override fun check(item: DisputeQuota): MutableList<String> {
        if (item.pinned && item.etaMinutes > limit) {
            return mutableListOf<String>("eta minutes ${item.etaMinutes} exceeds $limit while pinned")
        }
        return mutableListOf<String>()
    }
}

data class DisputeQuotaDepotLengthRule(var maxLength: Int) : DisputeQuotaRule {
    override fun check(item: DisputeQuota): MutableList<String> {
        val size = item.depot.length
        if (size > maxLength) {
            return mutableListOf<String>("depot is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a dispute quota. */
data class DisputeQuotaValidator(var rules: MutableList<DisputeQuotaRule>) {
    fun validate(item: DisputeQuota): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: DisputeQuota) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DisputeQuota>): MutableMap<String, MutableList<String>> {
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

fun defaultDisputeQuotaValidator(): DisputeQuotaValidator {
    return DisputeQuotaValidator(
        mutableListOf<DisputeQuotaRule>(
            DisputeQuotaTitleLengthRule(64),
            DisputeQuotaRateRangeRule(9.7, 4335.9),
            DisputeQuotaPinnedGuardRule(1000),
            DisputeQuotaDepotLengthRule(3600),
        ),
    )
}
