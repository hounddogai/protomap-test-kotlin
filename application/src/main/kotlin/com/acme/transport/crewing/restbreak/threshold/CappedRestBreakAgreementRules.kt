package com.acme.transport.crewing.restbreak.threshold

import com.acme.transport.kit.text.normalize

/** A rule a capped rest break agreement must pass. Check returns its problems, or none. */
interface CappedRestBreakAgreementRule {
    fun check(item: CappedRestBreakAgreement): MutableList<String>
}

data class CappedRestBreakAgreementCodeLengthRule(var maxLength: Int) : CappedRestBreakAgreementRule {
    override fun check(item: CappedRestBreakAgreement): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CappedRestBreakAgreementCodePrefixRule(var prefix: String) : CappedRestBreakAgreementRule {
    override fun check(item: CappedRestBreakAgreement): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class CappedRestBreakAgreementAllowedCodeRule(
    var allowed: MutableList<String>,
) : CappedRestBreakAgreementRule {
    override fun check(item: CappedRestBreakAgreement): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is inconsistent")
        }
        return mutableListOf<String>()
    }
}

data class CappedRestBreakAgreementMaximumRangeRule(
    var min: Double,
    var max: Double,
) : CappedRestBreakAgreementRule {
    override fun check(item: CappedRestBreakAgreement): MutableList<String> {
        val value = item.maximum
        if (value < min || value > max) {
            return mutableListOf<String>("maximum $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class CappedRestBreakAgreementLabelLengthRule(var maxLength: Int) : CappedRestBreakAgreementRule {
    override fun check(item: CappedRestBreakAgreement): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CappedRestBreakAgreementMarginRangeRule(
    var min: Double,
    var max: Double,
) : CappedRestBreakAgreementRule {
    override fun check(item: CappedRestBreakAgreement): MutableList<String> {
        val value = item.margin
        if (value < min || value > max) {
            return mutableListOf<String>("margin $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a capped rest break agreement. */
data class CappedRestBreakAgreementValidator(var rules: MutableList<CappedRestBreakAgreementRule>) {
    fun validate(item: CappedRestBreakAgreement): MutableList<String> {
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

    fun requireValid(item: CappedRestBreakAgreement) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CappedRestBreakAgreement>): MutableMap<String, MutableList<String>> {
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

fun defaultCappedRestBreakAgreementValidator(): CappedRestBreakAgreementValidator {
    return CappedRestBreakAgreementValidator(
        mutableListOf<CappedRestBreakAgreementRule>(
            CappedRestBreakAgreementCodeLengthRule(86400),
            CappedRestBreakAgreementCodePrefixRule("OPS"),
            CappedRestBreakAgreementAllowedCodeRule(mutableListOf<String>("capped", "regional", "adaptive")),
            CappedRestBreakAgreementMaximumRangeRule(8.6, 112.9),
            CappedRestBreakAgreementLabelLengthRule(16),
            CappedRestBreakAgreementMarginRangeRule(9.4, 1593.0),
        ),
    )
}
