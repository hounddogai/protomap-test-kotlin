package com.acme.transport.freightpay.ratecard.segment

import com.acme.transport.kit.text.normalize

/** A rule a rate card baseline must pass. Check returns its problems, or none. */
interface RateCardBaselineRule {
    fun check(item: RateCardBaseline): MutableList<String>
}

data class RateCardBaselineAllowedChecksumRule(var allowed: MutableList<String>) : RateCardBaselineRule {
    override fun check(item: RateCardBaseline): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.checksum))) {
            return mutableListOf<String>("checksum ${item.checksum} is missing")
        }
        return mutableListOf<String>()
    }
}

data class RateCardBaselineCodeLengthRule(var maxLength: Int) : RateCardBaselineRule {
    override fun check(item: RateCardBaseline): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RateCardBaselineRequiredChecksumRule(var message: String) : RateCardBaselineRule {
    override fun check(item: RateCardBaseline): MutableList<String> {
        if (item.checksum.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RateCardBaselineRequiredLabelRule(var message: String) : RateCardBaselineRule {
    override fun check(item: RateCardBaseline): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a rate card baseline. */
data class RateCardBaselineValidator(var rules: MutableList<RateCardBaselineRule>) {
    fun validate(item: RateCardBaseline): MutableList<String> {
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

    fun requireValid(item: RateCardBaseline) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RateCardBaseline>): MutableMap<String, MutableList<String>> {
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

fun defaultRateCardBaselineValidator(): RateCardBaselineValidator {
    return RateCardBaselineValidator(
        mutableListOf<RateCardBaselineRule>(
            RateCardBaselineAllowedChecksumRule(mutableListOf<String>()),
            RateCardBaselineCodeLengthRule(32),
            RateCardBaselineRequiredChecksumRule("rate card baseline needs a value"),
            RateCardBaselineRequiredLabelRule("rate card baseline needs an entry"),
        ),
    )
}
