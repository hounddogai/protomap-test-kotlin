package com.acme.transport.freightpay.ratecard.cardledger

import com.acme.transport.kit.text.normalize

/** A rule a lenient rate card template must pass. Check returns its problems, or none. */
interface LenientRateCardTemplateRule {
    fun check(item: LenientRateCardTemplate): MutableList<String>
}

data class LenientRateCardTemplateRatioRangeRule(
    var min: Double,
    var max: Double,
) : LenientRateCardTemplateRule {
    override fun check(item: LenientRateCardTemplate): MutableList<String> {
        val value = item.ratio
        if (value < min || value > max) {
            return mutableListOf<String>("ratio $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class LenientRateCardTemplateEtaMinutesRangeRule(
    var min: Int,
    var max: Int,
) : LenientRateCardTemplateRule {
    override fun check(item: LenientRateCardTemplate): MutableList<String> {
        val value = item.etaMinutes
        if (value < min || value > max) {
            return mutableListOf<String>("eta minutes $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class LenientRateCardTemplateCodeLengthRule(var maxLength: Int) : LenientRateCardTemplateRule {
    override fun check(item: LenientRateCardTemplate): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LenientRateCardTemplateBatchSizeRangeRule(var min: Int, var max: Int) : LenientRateCardTemplateRule {
    override fun check(item: LenientRateCardTemplate): MutableList<String> {
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

data class LenientRateCardTemplateAllowedCodeRule(
    var allowed: MutableList<String>,
) : LenientRateCardTemplateRule {
    override fun check(item: LenientRateCardTemplate): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is above maximum")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a lenient rate card template. */
data class LenientRateCardTemplateValidator(var rules: MutableList<LenientRateCardTemplateRule>) {
    fun validate(item: LenientRateCardTemplate): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: LenientRateCardTemplate) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LenientRateCardTemplate>): MutableMap<String, MutableList<String>> {
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

fun defaultLenientRateCardTemplateValidator(): LenientRateCardTemplateValidator {
    return LenientRateCardTemplateValidator(
        mutableListOf<LenientRateCardTemplateRule>(
            LenientRateCardTemplateRatioRangeRule(0.8, 1434.5),
            LenientRateCardTemplateEtaMinutesRangeRule(255, 255),
            LenientRateCardTemplateCodeLengthRule(32),
            LenientRateCardTemplateBatchSizeRangeRule(80, 3600),
            LenientRateCardTemplateAllowedCodeRule(mutableListOf<String>("fallback", "tiered", "pending")),
        ),
    )
}
