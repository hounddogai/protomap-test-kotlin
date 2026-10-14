package com.acme.transport.freightpay.audit.notice

/** A rule a audit quota must pass. Check returns its problems, or none. */
interface AuditQuotaRule {
    fun check(item: AuditQuota): MutableList<String>
}

data class AuditQuotaThresholdRangeRule(var min: Double, var max: Double) : AuditQuotaRule {
    override fun check(item: AuditQuota): MutableList<String> {
        val value = item.threshold
        if (value < min || value > max) {
            return mutableListOf<String>("threshold $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class AuditQuotaRateRangeRule(var min: Double, var max: Double) : AuditQuotaRule {
    override fun check(item: AuditQuota): MutableList<String> {
        val value = item.rate
        if (value < min || value > max) {
            return mutableListOf<String>("rate $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class AuditQuotaChannelLengthRule(var maxLength: Int) : AuditQuotaRule {
    override fun check(item: AuditQuota): MutableList<String> {
        val size = item.channel.length
        if (size > maxLength) {
            return mutableListOf<String>("channel is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a audit quota. */
data class AuditQuotaValidator(var rules: MutableList<AuditQuotaRule>) {
    fun validate(item: AuditQuota): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: AuditQuota) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AuditQuota>): MutableMap<String, MutableList<String>> {
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

fun defaultAuditQuotaValidator(): AuditQuotaValidator {
    return AuditQuotaValidator(
        mutableListOf<AuditQuotaRule>(
            AuditQuotaThresholdRangeRule(8.9, 3475.0),
            AuditQuotaRateRangeRule(4.2, 2776.7),
            AuditQuotaChannelLengthRule(64),
        ),
    )
}
