package com.acme.transport.customs.declaration.notice

import com.acme.transport.kit.text.normalize

/** A rule a declaration notice must pass. Check returns its problems, or none. */
interface DeclarationNoticeRule {
    fun check(item: DeclarationNotice): MutableList<String>
}

data class DeclarationNoticeRequiredCountryRule(var message: String) : DeclarationNoticeRule {
    override fun check(item: DeclarationNotice): MutableList<String> {
        if (item.country.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DeclarationNoticeAllowedCodeRule(var allowed: MutableList<String>) : DeclarationNoticeRule {
    override fun check(item: DeclarationNotice): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is stale")
        }
        return mutableListOf<String>()
    }
}

data class DeclarationNoticeWindowDaysRangeRule(var min: Int, var max: Int) : DeclarationNoticeRule {
    override fun check(item: DeclarationNotice): MutableList<String> {
        val value = item.windowDays
        if (value < min || value > max) {
            return mutableListOf<String>("window days $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class DeclarationNoticeCodeLengthRule(var maxLength: Int) : DeclarationNoticeRule {
    override fun check(item: DeclarationNotice): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DeclarationNoticeCodePrefixRule(var prefix: String) : DeclarationNoticeRule {
    override fun check(item: DeclarationNotice): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class DeclarationNoticeAmountRangeRule(var min: Double, var max: Double) : DeclarationNoticeRule {
    override fun check(item: DeclarationNotice): MutableList<String> {
        val value = item.amount
        if (value < min || value > max) {
            return mutableListOf<String>("amount $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a declaration notice. */
data class DeclarationNoticeValidator(var rules: MutableList<DeclarationNoticeRule>) {
    fun validate(item: DeclarationNotice): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: DeclarationNotice) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DeclarationNotice>): MutableMap<String, MutableList<String>> {
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

fun defaultDeclarationNoticeValidator(): DeclarationNoticeValidator {
    return DeclarationNoticeValidator(
        mutableListOf<DeclarationNoticeRule>(
            DeclarationNoticeRequiredCountryRule("declaration notice needs a value"),
            DeclarationNoticeAllowedCodeRule(mutableListOf<String>("regional", "tiered")),
            DeclarationNoticeWindowDaysRangeRule(16, 1000),
            DeclarationNoticeCodeLengthRule(1000),
            DeclarationNoticeCodePrefixRule("Q"),
            DeclarationNoticeAmountRangeRule(1.5, 4545.6),
        ),
    )
}
