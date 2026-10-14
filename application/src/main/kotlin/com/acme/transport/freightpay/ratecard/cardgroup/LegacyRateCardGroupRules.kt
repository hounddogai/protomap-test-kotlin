package com.acme.transport.freightpay.ratecard.cardgroup

import com.acme.transport.kit.text.normalize

/** A rule a legacy rate card group must pass. Check returns its problems, or none. */
interface LegacyRateCardGroupRule {
    fun check(item: LegacyRateCardGroup): MutableList<String>
}

data class LegacyRateCardGroupCodeLengthRule(var maxLength: Int) : LegacyRateCardGroupRule {
    override fun check(item: LegacyRateCardGroup): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LegacyRateCardGroupEtaMinutesRangeRule(var min: Int, var max: Int) : LegacyRateCardGroupRule {
    override fun check(item: LegacyRateCardGroup): MutableList<String> {
        val value = item.etaMinutes
        if (value < min || value > max) {
            return mutableListOf<String>("eta minutes $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class LegacyRateCardGroupAllowedCodeRule(var allowed: MutableList<String>) : LegacyRateCardGroupRule {
    override fun check(item: LegacyRateCardGroup): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is stale")
        }
        return mutableListOf<String>()
    }
}

data class LegacyRateCardGroupRequiredNameRule(var message: String) : LegacyRateCardGroupRule {
    override fun check(item: LegacyRateCardGroup): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a legacy rate card group. */
data class LegacyRateCardGroupValidator(var rules: MutableList<LegacyRateCardGroupRule>) {
    fun validate(item: LegacyRateCardGroup): MutableList<String> {
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

    fun requireValid(item: LegacyRateCardGroup) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LegacyRateCardGroup>): MutableMap<String, MutableList<String>> {
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

fun defaultLegacyRateCardGroupValidator(): LegacyRateCardGroupValidator {
    return LegacyRateCardGroupValidator(
        mutableListOf<LegacyRateCardGroupRule>(
            LegacyRateCardGroupCodeLengthRule(80),
            LegacyRateCardGroupEtaMinutesRangeRule(1000, 86400),
            LegacyRateCardGroupAllowedCodeRule(mutableListOf<String>()),
            LegacyRateCardGroupRequiredNameRule("legacy rate card group needs a value"),
        ),
    )
}
