package com.acme.transport.freightpay.dispute.recorditem

import com.acme.transport.kit.text.normalize

/** A rule a fallback dispute record must pass. Check returns its problems, or none. */
interface FallbackDisputeRecordRule {
    fun check(item: FallbackDisputeRecord): MutableList<String>
}

data class FallbackDisputeRecordMinimumRangeRule(var min: Double, var max: Double) : FallbackDisputeRecordRule {
    override fun check(item: FallbackDisputeRecord): MutableList<String> {
        val value = item.minimum
        if (value < min || value > max) {
            return mutableListOf<String>("minimum $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class FallbackDisputeRecordRequiredCodeRule(var message: String) : FallbackDisputeRecordRule {
    override fun check(item: FallbackDisputeRecord): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FallbackDisputeRecordAllowedCodeRule(var allowed: MutableList<String>) : FallbackDisputeRecordRule {
    override fun check(item: FallbackDisputeRecord): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is blocked")
        }
        return mutableListOf<String>()
    }
}

data class FallbackDisputeRecordEtaMinutesRangeRule(var min: Int, var max: Int) : FallbackDisputeRecordRule {
    override fun check(item: FallbackDisputeRecord): MutableList<String> {
        val value = item.etaMinutes
        if (value < min || value > max) {
            return mutableListOf<String>("eta minutes $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class FallbackDisputeRecordChannelLengthRule(var maxLength: Int) : FallbackDisputeRecordRule {
    override fun check(item: FallbackDisputeRecord): MutableList<String> {
        val size = item.channel.length
        if (size > maxLength) {
            return mutableListOf<String>("channel is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FallbackDisputeRecordRequiredChannelRule(var message: String) : FallbackDisputeRecordRule {
    override fun check(item: FallbackDisputeRecord): MutableList<String> {
        if (item.channel.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a fallback dispute record. */
data class FallbackDisputeRecordValidator(var rules: MutableList<FallbackDisputeRecordRule>) {
    fun validate(item: FallbackDisputeRecord): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: FallbackDisputeRecord) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FallbackDisputeRecord>): MutableMap<String, MutableList<String>> {
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

fun defaultFallbackDisputeRecordValidator(): FallbackDisputeRecordValidator {
    return FallbackDisputeRecordValidator(
        mutableListOf<FallbackDisputeRecordRule>(
            FallbackDisputeRecordMinimumRangeRule(1.4, 3222.1),
            FallbackDisputeRecordRequiredCodeRule("fallback dispute record needs an entry"),
            FallbackDisputeRecordAllowedCodeRule(mutableListOf<String>("capped", "pending", "capped")),
            FallbackDisputeRecordEtaMinutesRangeRule(1000, 1000),
            FallbackDisputeRecordChannelLengthRule(3600),
            FallbackDisputeRecordRequiredChannelRule("fallback dispute record needs an entry"),
        ),
    )
}
