package com.acme.transport.freightpay.dispute.disputetemplate

import com.acme.transport.kit.text.normalize

/** A rule a lenient dispute template must pass. Check returns its problems, or none. */
interface LenientDisputeTemplateRule {
    fun check(item: LenientDisputeTemplate): MutableList<String>
}

data class LenientDisputeTemplateCodeLengthRule(var maxLength: Int) : LenientDisputeTemplateRule {
    override fun check(item: LenientDisputeTemplate): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LenientDisputeTemplateAllowedTitleRule(
    var allowed: MutableList<String>,
) : LenientDisputeTemplateRule {
    override fun check(item: LenientDisputeTemplate): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.title))) {
            return mutableListOf<String>("title ${item.title} is below minimum")
        }
        return mutableListOf<String>()
    }
}

data class LenientDisputeTemplateTitleLengthRule(var maxLength: Int) : LenientDisputeTemplateRule {
    override fun check(item: LenientDisputeTemplate): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LenientDisputeTemplateDockLengthRule(var maxLength: Int) : LenientDisputeTemplateRule {
    override fun check(item: LenientDisputeTemplate): MutableList<String> {
        val size = item.dock.length
        if (size > maxLength) {
            return mutableListOf<String>("dock is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LenientDisputeTemplateAmountRangeRule(
    var min: Double,
    var max: Double,
) : LenientDisputeTemplateRule {
    override fun check(item: LenientDisputeTemplate): MutableList<String> {
        val value = item.amount
        if (value < min || value > max) {
            return mutableListOf<String>("amount $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class LenientDisputeTemplateExpiresAtRangeRule(var min: Int, var max: Int) : LenientDisputeTemplateRule {
    override fun check(item: LenientDisputeTemplate): MutableList<String> {
        val value = item.expiresAt
        if (value < min || value > max) {
            return mutableListOf<String>("expires at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a lenient dispute template. */
data class LenientDisputeTemplateValidator(var rules: MutableList<LenientDisputeTemplateRule>) {
    fun validate(item: LenientDisputeTemplate): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: LenientDisputeTemplate) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LenientDisputeTemplate>): MutableMap<String, MutableList<String>> {
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

fun defaultLenientDisputeTemplateValidator(): LenientDisputeTemplateValidator {
    return LenientDisputeTemplateValidator(
        mutableListOf<LenientDisputeTemplateRule>(
            LenientDisputeTemplateCodeLengthRule(120),
            LenientDisputeTemplateAllowedTitleRule(mutableListOf<String>("seasonal", "scheduled")),
            LenientDisputeTemplateTitleLengthRule(86400),
            LenientDisputeTemplateDockLengthRule(32),
            LenientDisputeTemplateAmountRangeRule(3.0, 379.6),
            LenientDisputeTemplateExpiresAtRangeRule(80, 120),
        ),
    )
}
