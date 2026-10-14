package com.acme.transport.lastmile.redelivery.threshold

import com.acme.transport.kit.text.normalize

/** A rule a redelivery notice must pass. Check returns its problems, or none. */
interface RedeliveryNoticeRule {
    fun check(item: RedeliveryNotice): MutableList<String>
}

data class RedeliveryNoticeCodeLengthRule(var maxLength: Int) : RedeliveryNoticeRule {
    override fun check(item: RedeliveryNotice): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RedeliveryNoticeCountryLengthRule(var maxLength: Int) : RedeliveryNoticeRule {
    override fun check(item: RedeliveryNotice): MutableList<String> {
        val size = item.country.length
        if (size > maxLength) {
            return mutableListOf<String>("country is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RedeliveryNoticeStartsAtRangeRule(var min: Int, var max: Int) : RedeliveryNoticeRule {
    override fun check(item: RedeliveryNotice): MutableList<String> {
        val value = item.startsAt
        if (value < min || value > max) {
            return mutableListOf<String>("starts at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class RedeliveryNoticeAllowedCountryRule(var allowed: MutableList<String>) : RedeliveryNoticeRule {
    override fun check(item: RedeliveryNotice): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.country))) {
            return mutableListOf<String>("country ${item.country} is blocked")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a redelivery notice. */
data class RedeliveryNoticeValidator(var rules: MutableList<RedeliveryNoticeRule>) {
    fun validate(item: RedeliveryNotice): MutableList<String> {
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

    fun requireValid(item: RedeliveryNotice) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RedeliveryNotice>): MutableMap<String, MutableList<String>> {
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

fun defaultRedeliveryNoticeValidator(): RedeliveryNoticeValidator {
    return RedeliveryNoticeValidator(
        mutableListOf<RedeliveryNoticeRule>(
            RedeliveryNoticeCodeLengthRule(64),
            RedeliveryNoticeCountryLengthRule(86400),
            RedeliveryNoticeStartsAtRangeRule(16, 32),
            RedeliveryNoticeAllowedCountryRule(mutableListOf<String>("strict", "batched")),
        ),
    )
}
