package com.acme.transport.fleetops.tire.milestone

import com.acme.transport.kit.text.normalize

/** A rule a lenient tire request must pass. Check returns its problems, or none. */
interface LenientTireRequestRule {
    fun check(item: LenientTireRequest): MutableList<String>
}

data class LenientTireRequestCodeLengthRule(var maxLength: Int) : LenientTireRequestRule {
    override fun check(item: LenientTireRequest): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LenientTireRequestScoreRangeRule(var min: Double, var max: Double) : LenientTireRequestRule {
    override fun check(item: LenientTireRequest): MutableList<String> {
        val value = item.score
        if (value < min || value > max) {
            return mutableListOf<String>("score $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class LenientTireRequestCodePrefixRule(var prefix: String) : LenientTireRequestRule {
    override fun check(item: LenientTireRequest): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class LenientTireRequestRequiredCountryRule(var message: String) : LenientTireRequestRule {
    override fun check(item: LenientTireRequest): MutableList<String> {
        if (item.country.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LenientTireRequestTitleLengthRule(var maxLength: Int) : LenientTireRequestRule {
    override fun check(item: LenientTireRequest): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LenientTireRequestAllowedCodeRule(var allowed: MutableList<String>) : LenientTireRequestRule {
    override fun check(item: LenientTireRequest): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is out of range")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a lenient tire request. */
data class LenientTireRequestValidator(var rules: MutableList<LenientTireRequestRule>) {
    fun validate(item: LenientTireRequest): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: LenientTireRequest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LenientTireRequest>): MutableMap<String, MutableList<String>> {
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

fun defaultLenientTireRequestValidator(): LenientTireRequestValidator {
    return LenientTireRequestValidator(
        mutableListOf<LenientTireRequestRule>(
            LenientTireRequestCodeLengthRule(120),
            LenientTireRequestScoreRangeRule(2.4, 4369.2),
            LenientTireRequestCodePrefixRule("Q"),
            LenientTireRequestRequiredCountryRule("lenient tire request needs an entry"),
            LenientTireRequestTitleLengthRule(1000),
            LenientTireRequestAllowedCodeRule(mutableListOf<String>("rolling", "regional", "weighted")),
        ),
    )
}
