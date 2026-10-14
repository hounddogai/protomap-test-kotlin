package com.acme.transport.emissions.evcharge.chargeslot

/** A rule a ev charge quota must pass. Check returns its problems, or none. */
interface EvChargeQuotaRule {
    fun check(item: EvChargeQuota): MutableList<String>
}

data class EvChargeQuotaCodePrefixRule(var prefix: String) : EvChargeQuotaRule {
    override fun check(item: EvChargeQuota): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class EvChargeQuotaEtaMinutesRangeRule(var min: Int, var max: Int) : EvChargeQuotaRule {
    override fun check(item: EvChargeQuota): MutableList<String> {
        val value = item.etaMinutes
        if (value < min || value > max) {
            return mutableListOf<String>("eta minutes $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class EvChargeQuotaChecksumLengthRule(var maxLength: Int) : EvChargeQuotaRule {
    override fun check(item: EvChargeQuota): MutableList<String> {
        val size = item.checksum.length
        if (size > maxLength) {
            return mutableListOf<String>("checksum is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a ev charge quota. */
data class EvChargeQuotaValidator(var rules: MutableList<EvChargeQuotaRule>) {
    fun validate(item: EvChargeQuota): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: EvChargeQuota) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<EvChargeQuota>): MutableMap<String, MutableList<String>> {
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

fun defaultEvChargeQuotaValidator(): EvChargeQuotaValidator {
    return EvChargeQuotaValidator(
        mutableListOf<EvChargeQuotaRule>(
            EvChargeQuotaCodePrefixRule("Q"),
            EvChargeQuotaEtaMinutesRangeRule(120, 120),
            EvChargeQuotaChecksumLengthRule(120),
        ),
    )
}
