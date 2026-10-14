package com.acme.transport.lastmile.attempt.manifest

import com.acme.transport.kit.text.normalize

/** A rule a attempt notice must pass. Check returns its problems, or none. */
interface AttemptNoticeRule {
    fun check(item: AttemptNotice): MutableList<String>
}

data class AttemptNoticeDiscountRangeRule(var min: Double, var max: Double) : AttemptNoticeRule {
    override fun check(item: AttemptNotice): MutableList<String> {
        val value = item.discount
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("discount $value is below $min")
        }
        if (value > max) {
            problems.add("discount $value is above $max")
        }
        return problems
    }
}

data class AttemptNoticeRequiredCountryRule(var message: String) : AttemptNoticeRule {
    override fun check(item: AttemptNotice): MutableList<String> {
        if (item.country.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AttemptNoticeRequiredCodeRule(var message: String) : AttemptNoticeRule {
    override fun check(item: AttemptNotice): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AttemptNoticeAllowedCodeRule(var allowed: MutableList<String>) : AttemptNoticeRule {
    override fun check(item: AttemptNotice): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is not allowed")
        }
        return mutableListOf<String>()
    }
}

data class AttemptNoticeCodePrefixRule(var prefix: String) : AttemptNoticeRule {
    override fun check(item: AttemptNotice): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class AttemptNoticeCountryLengthRule(var maxLength: Int) : AttemptNoticeRule {
    override fun check(item: AttemptNotice): MutableList<String> {
        val size = item.country.length
        if (size > maxLength) {
            return mutableListOf<String>("country is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a attempt notice. */
data class AttemptNoticeValidator(var rules: MutableList<AttemptNoticeRule>) {
    fun validate(item: AttemptNotice): MutableList<String> {
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

    fun requireValid(item: AttemptNotice) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AttemptNotice>): MutableMap<String, MutableList<String>> {
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

fun defaultAttemptNoticeValidator(): AttemptNoticeValidator {
    return AttemptNoticeValidator(
        mutableListOf<AttemptNoticeRule>(
            AttemptNoticeDiscountRangeRule(8.1, 302.4),
            AttemptNoticeRequiredCountryRule("attempt notice needs an entry"),
            AttemptNoticeRequiredCodeRule("attempt notice needs this field"),
            AttemptNoticeAllowedCodeRule(mutableListOf<String>("weighted")),
            AttemptNoticeCodePrefixRule("REF"),
            AttemptNoticeCountryLengthRule(120),
        ),
    )
}
